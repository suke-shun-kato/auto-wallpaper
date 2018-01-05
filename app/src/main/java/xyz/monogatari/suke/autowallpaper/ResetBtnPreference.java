package xyz.monogatari.suke.autowallpaper;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
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
    private String textDialog;
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
            this.textDialog
                    = typedAry.getString(R.styleable.ResetBtnPreference_textDialog);
            this.textResult
                    = typedAry.getString(R.styleable.ResetBtnPreference_textResult);
        } finally {
            typedAry.recycle();
        }
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
            // SharedPreferenceの値を削除（空になる）
            PreferenceManager
                    .getDefaultSharedPreferences(this.getContext())
                    .edit()
                    .clear()
                    .apply();

            // SharedPreferenceにデフォルト値を設定（SharedPreferenceに値があるときは設定されない）
            PreferenceManager.setDefaultValues(this.getContext(), R.xml.preferences, true);

            Toast.makeText(this.getContext(), this.textResult, Toast.LENGTH_SHORT)
                    .show();
        }
    }
}
