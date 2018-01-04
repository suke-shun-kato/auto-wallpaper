package xyz.monogatari.suke.autowallpaper;

import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.util.Log;

/**
 * Created by k-shunsuke on 2018/01/01.
 */

public class StartTimingPreference extends ListPreference {
    public StartTimingPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StartTimingPreference(Context context) {
        super(context);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

Log.d("○"+this.getClass().getSimpleName(), "onDialogClosed() : " + this.hashCode());
Log.d("○"+this.getClass().getSimpleName(), ""+ "value:" + this.getValue() + ", result:" + positiveResult);

        // OKボタンを押してダイアログを閉じたとき選択ディレクトリパスを保存する
        if (positiveResult) {
            // 設定値を保存
            long unixTimeMsec = System.currentTimeMillis() + Integer.parseInt(this.getValue());
            this.persistString(String.valueOf(unixTimeMsec));
        }
    }
}
