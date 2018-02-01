package xyz.monogatari.suke.autowallpaper.util;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by k-shunsuke on 2018/02/01.
 */

public class WpManagerService extends IntentService {
    public WpManagerService(String name) {
        super(name);
Log.d("○△△"+ this.getClass().getSimpleName(), "WpManagerService(name), スレッド名:" + Thread.currentThread().getName());
    }

    public WpManagerService() {
        super("WpManagerService");
Log.d("○△△" + this.getClass().getSimpleName(), "WpManagerService(), スレッド名:" + Thread.currentThread().getName());
    }

    public static final String ACTION_NAME = "WpManagerService";
    public static final String KEY_NAME = "state";
    public static final int STATE_START = 1;
    public static final int STATE_DESTROY = 2;

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
Log.d("○△△" + this.getClass().getSimpleName(), "onStartCommand(), スレッド名:" + Thread.currentThread().getName());

        Intent i = new Intent(ACTION_NAME);
        i.putExtra(KEY_NAME, STATE_START);
        this.sendBroadcast(i);

        return super.onStartCommand(intent, flags, startId);
    }

    /************************************
     * ここだけ別スレッドで実行される（他はメインスレッドで実行される）
     */
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
Log.d("○△△" + this.getClass().getSimpleName(), "onHandleIntent(), スレッド名:" + Thread.currentThread().getName());
        // 別スレッドで実行されているからそのまま壁紙変更
        new ImgGetPorcSet(this).execute();
    }



    @Override
    public void onDestroy() {
        super.onDestroy();

        Intent i = new Intent(ACTION_NAME);
        i.putExtra(KEY_NAME, STATE_DESTROY);
        this.sendBroadcast(i);
Log.d("○△△" + this.getClass().getSimpleName(), "onDestroy(), スレッド名:" + Thread.currentThread().getName());
    }
}
