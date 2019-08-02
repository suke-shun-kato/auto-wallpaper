// TODO androidx対応させて復活させる
//package xyz.goodistory.autowallpaper.preference;
//
//import android.annotation.TargetApi;
//import android.app.Activity;
//import android.content.Context;
//import android.content.SharedPreferences;
//import android.content.res.TypedArray;
//import android.os.Build;
//import androidx.preference.DialogPreference;
//import androidx.preference.PreferenceManager;
//import android.util.AttributeSet;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Toast;
//
//import xyz.goodistory.autowallpaper.R;
//
///**
// * デフォルト値にリセットする用のボタン
// * Created by k-shunsuke on 2018/01/05.
// */
//
//public class ResetPreference extends DialogPreference {
//    // --------------------------------------------------------------------
//    // フィールド
//    // --------------------------------------------------------------------
//    /** カスタム属性からの文字列の設定 */
//    private String mTextDialogMsg;
//    private String mTextDialogTitle;
//
//    /** 初期化した後に表示されるトーストの文字 */
//    private String mTextResult;
//
//    // --------------------------------------------------------------------
//    // コンストラクタ
//    // --------------------------------------------------------------------
//    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//    public ResetPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
//        super(context, attrs, defStyleAttr, defStyleRes);
//        setAttributeSet(context, attrs);
//    }
//
//    public ResetPreference(Context context, AttributeSet attrs, int defStyleAttr) {
//        super(context, attrs, defStyleAttr);
//        setAttributeSet(context, attrs);
//    }
//
//    public ResetPreference(Context context, AttributeSet attrs) {
//        super(context, attrs);
//        setAttributeSet(context, attrs);
//    }
//
//    /**
//     * XMLのカスタム属性をフィールドに読み込む
//     * @param context context
//     * @param attrs attrs
//     */
//    private void setAttributeSet(Context context, AttributeSet attrs){
//        TypedArray typedAry = context.getTheme().obtainStyledAttributes(
//                attrs, R.styleable.ResetPreference,
//                0, 0);
//        try {
//            mTextDialogMsg = typedAry.getString(R.styleable.ResetPreference_textDialogMsg);
//            mTextDialogTitle = typedAry.getString(R.styleable.ResetPreference_textDialogTitle);
//            mTextResult = typedAry.getString(R.styleable.ResetPreference_textResult);
//        } finally {
//            typedAry.recycle();
//        }
//    }
//    // --------------------------------------------------------------------
//    // メソッド、オーバーライド
//    // --------------------------------------------------------------------
//    /**
//     * このPreferenceのViewを作るとき
//     * @param parent このPreferenceのviewの親のView
//     * @return このPreferenceのView
//     */
//    @Override
//    protected View onCreateView(ViewGroup parent) {
//        setDialogTitle(mTextDialogTitle);
//        setDialogMessage(mTextDialogMsg);
//
//        return super.onCreateView(parent);
//    }
//
//
//    /**
//     * ダイアログが閉じたとき
//     * OKボタンを押したときSharedPreferenceを初期値に戻す
//     * @param positiveResult true:ユーザーがポジティブボタン（OKボタン）を押したとき,
//     *                      false:ネガティブボタン、またはキャンセルボタンを押したとき
//     */
//    @Override
//    protected void onDialogClosed(boolean positiveResult) {
//        Context context = getContext();
//
//        // OKボタンを押してダイアログを閉じたとき
//        if (positiveResult) {
//
//            // ----------------------------------
//            // spの値をリセット
//            // ----------------------------------
//            // SharedPreferenceの値を削除（空）にする、デフォルト値はセットされないので注意、
//            // ちなみにここでは OnSharedPreferenceChangeListener.onSharedPreferenceChanged は発火する
//            SharedPreferences.Editor editor= PreferenceManager
//                    .getDefaultSharedPreferences(context)
//                    .edit();
//            editor.clear().apply();
//
//            // preferences.xmlから、SharedPreferenceにデフォルト値を設定、
//            // SharedPreferenceの値を空にしないと動かない
//            PreferenceManager.setDefaultValues(context, R.xml.preferences, true);
//
//            // ----------------------------------
//            // 描画
//            // ----------------------------------
//            // 再描画
//            Activity activity = ((Activity)context);
//            activity.recreate();
//
//            // トースト表示
//            Toast.makeText(context, mTextResult, Toast.LENGTH_SHORT)
//                    .show();
//        }
//    }
//
//}
