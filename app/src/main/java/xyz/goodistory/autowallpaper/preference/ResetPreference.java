package xyz.goodistory.autowallpaper.preference;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;

import androidx.annotation.Nullable;
import androidx.preference.DialogPreference;
import androidx.preference.PreferenceDialogFragmentCompat;
import androidx.preference.PreferenceManager;

import android.os.Bundle;
import android.util.AttributeSet;
import android.widget.Toast;

import xyz.goodistory.autowallpaper.R;

/**
 * デフォルト値にリセットする用のボタン
 * Created by k-shunsuke on 2018/01/05.
 */

public class ResetPreference extends DialogPreference {
    // --------------------------------------------------------------------
    // フィールド
    // --------------------------------------------------------------------
    /** 初期化した後に表示されるトーストの文字, nullの場合は表示しない */
    @Nullable private String mTextResult;

    // --------------------------------------------------------------------
    // コンストラクタ
    // --------------------------------------------------------------------
    public ResetPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setAttributeSet(context, attrs);
    }

    public ResetPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setAttributeSet(context, attrs);
    }

    public ResetPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setAttributeSet(context, attrs);
    }

    /**
     * XMLのカスタム属性をフィールドに読み込む
     * @param context context
     * @param attrs attrs
     */
    private void setAttributeSet(Context context, AttributeSet attrs){
        TypedArray typedAry = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.ResetPreference,
                0, 0);
        try {
            mTextResult = typedAry.getString(R.styleable.ResetPreference_textResult);
        } catch (RuntimeException e) {
            mTextResult = null;
        } finally {
            typedAry.recycle();
        }
    }



    // --------------------------------------------------------------------
    // getter
    // --------------------------------------------------------------------
    private String getTextResult() {
        return mTextResult;
    }

    // --------------------------------------------------------------------
    // class
    // --------------------------------------------------------------------
    public static class Dialog extends PreferenceDialogFragmentCompat {
        /**
         * preferenceのkey名を保存してインスタンスを取得,
         * 他のPreferenceDialogFragmentと同じ書き方
         * @param key このダイアログをshowするPreferenceのkey名
         * @return インスタンス
         */
        public static Dialog newInstance(String key) {
            // インスタンス作成
            final Dialog dialog = new Dialog();

            // Preferenceのkey名をfragment付きのBundleに保存, destroyされても保存される
            final Bundle b = new Bundle(1);
            b.putString(ARG_KEY, key);
            dialog.setArguments(b);

            return dialog;
        }

        @Override
        public void onDialogClosed(boolean positiveResult) {
            // ----------------------------------
            // 初期処理
            // ----------------------------------
            if (!positiveResult) {
                return;
            }

            Context context = getContext();
            if (context == null) {
                throw new IllegalStateException("can't get context");
            }

            // ----------------------------------
            // SharedPreference の値をリセット
            // ----------------------------------
            // SharedPreferenceの値を削除（空）にする、デフォルト値はセットされないので注意、
            // ちなみにここでは OnSharedPreferenceChangeListener.onSharedPreferenceChanged は発火する
            SharedPreferences.Editor editor= PreferenceManager
                    .getDefaultSharedPreferences(context)
                    .edit();
            editor.clear().apply();

            // preferences.xmlから、SharedPreferenceにデフォルト値を設定、
            // SharedPreferenceの値を空にしないと動かない
            PreferenceManager.setDefaultValues(context, R.xml.preferences, true);

            // ----------------------------------
            // 描画
            // ----------------------------------
            // 再描画
            Activity activity = ((Activity)context);
            activity.recreate();

            // ----------------------------------
            // トースト表示
            // ----------------------------------
            ResetPreference resetPreference = (ResetPreference)getPreference();
            String textResult = resetPreference.getTextResult();
            if (textResult != null) {
                Toast.makeText(context, textResult, Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }


}
