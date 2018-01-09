package xyz.monogatari.suke.autowallpaper;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;

/**
 * 開始タイミングのカスタムプリファレンス、最終的にここの値を読み込んで挙動している
 * Created by k-shunsuke on 2018/01/01.
 */
public class StartTimingPreference extends Preference {
    public StartTimingPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StartTimingPreference(Context context) {
        super(context);
    }

    // --------------------------------------------------------------------
    // 
    // --------------------------------------------------------------------
    /************************************
     *
     */
    public void setValue(double mag, long intervalMsec, long nowUnixTimeMsec) {
        this.persistLong( Math.round( mag * intervalMsec + nowUnixTimeMsec ) );
    }
}
