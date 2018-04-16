package xyz.monogatari.autowallpaper;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

/**
 * デフォルト値にリセットする用のボタン
 * Created by k-shunsuke on 2018/01/05.
 */

public class ResetBtnPreference extends DialogPreference {
    // --------------------------------------------------------------------
    // フィールド
    // --------------------------------------------------------------------
    /** カスタム属性からの文字列の設定 */
    private String textDialogMsg;
    private String textDialogTitle;
    /** 初期化した後に表示されるトーストの文字 */
    private String textResult;

    // --------------------------------------------------------------------
    // コンストラクタ
    // --------------------------------------------------------------------
    public ResetBtnPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.setField(context, attrs);
    }

    public ResetBtnPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setField(context, attrs);
    }

    private void setField(Context context, AttributeSet attrs){
        // ----------------------------------
        // XMLのカスタム属性をフィールドに読み込む
        // ----------------------------------
        TypedArray typedAry = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.ResetBtnPreference,
                0, 0);
        try {
            this.textDialogMsg
                    = typedAry.getString(R.styleable.ResetBtnPreference_textDialogMsg);
            this.textDialogTitle
                    = typedAry.getString(R.styleable.ResetBtnPreference_textDialogTitle);
            this.textResult
                    = typedAry.getString(R.styleable.ResetBtnPreference_textResult);
        } finally {
            typedAry.recycle();
        }
    }
    // --------------------------------------------------------------------
    // メソッド、オーバーライド
    // --------------------------------------------------------------------
    /************************************
     *
     */
    @Override
    protected View onCreateView(ViewGroup parent) {
        this.setDialogTitle(this.textDialogTitle);
        this.setDialogMessage(this.textDialogMsg);
        return super.onCreateView(parent);
    }


    /************************************
     * ダイアログが閉じたとき
     * OKボタンを押したときSharedPreferenceを初期値に戻す
     * @param positiveResult true:ユーザーがポジティブボタン（OKボタン）を押したとき,
     *                      false:ネガティブボタン、またはキャンセルボタンを押したとき
     */
    @Override
    protected void onDialogClosed(boolean positiveResult) {
Log.d("○"+this.getClass().getSimpleName(), "onDialogClosed(): positiveResult: " + positiveResult);


        // OKボタンを押してダイアログを閉じたとき
        if (positiveResult) {

            // ----------------------------------
            // spの値をリセット
            // ----------------------------------
            // SharedPreferenceの値を削除（実際にSharedPreferenceに値を適用するのはまだ）
            SharedPreferences.Editor editor= PreferenceManager
                    .getDefaultSharedPreferences(this.getContext())
                    .edit();
            // ここでspを空にする、デフォルト値はセットされないので注意、
            // ちなみにここでは OnSharedPreferenceChangeListener.onSharedPreferenceChanged は発火する
            // あと、クリアしないで直接デフォルト値を設定してapply()する方法を探したができなかった'18/4/6
            editor.clear().apply();

            // preferences.xmlから、SharedPreferenceにデフォルト値を設定
            // （SharedPreferenceに値があるときは設定されない）
            PreferenceManager.setDefaultValues(this.getContext(), R.xml.preferences, true);

            // ----------------------------------
            // 描画
            // ----------------------------------
            // 再描画
            Activity a = ((Activity)this.getContext());
            a.recreate();

            // トースト表示
            Toast.makeText(this.getContext(), this.textResult, Toast.LENGTH_SHORT)
                    .show();
        }
    }

}
