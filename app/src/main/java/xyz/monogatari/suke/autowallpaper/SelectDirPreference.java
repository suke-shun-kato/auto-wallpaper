package xyz.monogatari.suke.autowallpaper;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;

/**
 * ディレクトリを選択するPreference
 * Created by k-shunsuke on 2017/12/08.
 */
public class SelectDirPreference extends DialogPreference {
    // --------------------------------------------------------------------
    // フィールド
    // --------------------------------------------------------------------
    private static final int R_DIALOG_TITLE = R.string.select_dir_pref_dialog_title;

    // --------------------------------------------------------------------
    // コンストラクタ
    // --------------------------------------------------------------------
    public SelectDirPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
Log.d("○"+this.getClass().getSimpleName(), "コンストラクタ呼ばれた"+this.hashCode());

        // ダイアログのタイトルを設定
        this.setDialogTitle( this.getContext().getString(R_DIALOG_TITLE) );

        // OKボタンとCancelボタンの「文字列（テキスト）」を設置
        this.setPositiveButtonText(android.R.string.ok);
        this.setNegativeButtonText(android.R.string.cancel);

        // ダイアログのアイコンを設定、アイコンなし
        this.setDialogIcon(null);
    }

    // --------------------------------------------------------------------
    // メソッド
    // --------------------------------------------------------------------
    /************************************
     * 設定の値を保存する、ダイアログが閉じたとき
     * @param positiveResult true:ユーザーがポジティブボタン（OKボタン）を押したとき,
     *                      false:ネガティブボタン、またはキャンセルボタンを押したとき
     */
    @Override
    protected void onDialogClosed(boolean positiveResult) {
Log.d("○"+this.getClass().getSimpleName(), "onDialogClosed() が呼ばれた: " + positiveResult);
    }

    /************************************
     * 現在の値を初期化する、onGetDefaultValue()の後に呼ばれる
     * ※onGetDefaultValue() で値がreturnされないときは呼ばれない
     * @param restorePersistedValue true: 保存された設定値があるとき、false: ないとき
     * @param defaultValue デフォルト値、this.onGetDefaultValue()の戻り値（保存された値がある場合は常にnullになる）
     */
    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
Log.d("○"+this.getClass().getSimpleName(), "onSetInitialValue() が呼ばれた: " + restorePersistedValue);
    }

    /**********************************
     * onSetInitialValue()にデフォルト値を提供する, <Preference>が表示されたとき呼ばれる
     * ※デフォルト値が設定されていないときは呼ばれない
     * @param tArray <Preference>の属性の全ての配列
     * @param index  <Preference>の属性配列に対する「defaultValue」属性のインデックス
     * @return 初期値の文字列
     */
    @Override
    protected Object onGetDefaultValue(TypedArray tArray, int index) {
Log.d("○"+this.getClass().getSimpleName(), "onGetDefaultValue() が呼ばれた: " + tArray.getString(index));
        return tArray.getString(index);
//        return super.onGetDefaultValue(tArray, index);
    }

    /******************************
     * 回転直前に値を保存する
     * @return 保存する値
     */
    @Override
    protected Parcelable onSaveInstanceState() {
Log.d("○"+this.getClass().getSimpleName(), "onSaveInstanceState() が呼ばれた: " +this.hashCode());
        return super.onSaveInstanceState();
    }

    /***********************************
     * 回転直後に値を読み出す
     * @param state par
     */
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
Log.d("○"+this.getClass().getSimpleName(), "onRestoreInstanceState() が呼ばれた:"+this.hashCode());
    }

}
