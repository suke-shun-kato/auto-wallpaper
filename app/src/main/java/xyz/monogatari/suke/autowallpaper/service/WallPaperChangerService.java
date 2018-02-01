package xyz.monogatari.suke.autowallpaper.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import xyz.monogatari.suke.autowallpaper.SettingsFragment;
import xyz.monogatari.suke.autowallpaper.util.ImgGetPorcSet;

/**
 * 壁紙を変更するだけのサービス
 * Created by k-shunsuke on 2018/01/09.
 */

public class WallPaperChangerService extends IntentService {
    public WallPaperChangerService(String name) {
        super(name);
    }
    public WallPaperChangerService() {
        super("WallPaperChangerService");
    }
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        if ( sp.getBoolean(SettingsFragment.KEY_WHEN_TIMER, false) ) {
Log.d("○"+getClass().getSimpleName(), "onHandleIntent(): Alarmより起動:");
            // 別スレッドでサービス起動しているから、今のスレッドで壁紙交換
            new ImgGetPorcSet(this).execute();
        }
    }
}
