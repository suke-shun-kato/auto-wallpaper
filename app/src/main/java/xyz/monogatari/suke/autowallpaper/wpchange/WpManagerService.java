package xyz.monogatari.suke.autowallpaper.wpchange;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 壁紙を変更する用のサービス
 * Created by k-shunsuke on 2018/02/01.
 */
public class WpManagerService extends IntentService {
    // --------------------------------------------------------------------
    // 
    // --------------------------------------------------------------------
    public static final String ACTION_NAME = "xyz.monogatari.suke.autowallpaper.WP_SERVICE_ACTION";
    public static final String KEY_NAME = "state";
    public static final int STATE_START = 1;
    public static final int STATE_DESTROY = 2;
    public static final int STATE_ERROR = 3;


    private Timer timer;

    // --------------------------------------------------------------------
    // 
    // --------------------------------------------------------------------
    public WpManagerService(String name) {
        super(name);
Log.d("○△"+ this.getClass().getSimpleName(), "WpManagerService(name), スレッド名:" + Thread.currentThread().getName());
    }

    public WpManagerService() {
        super("WpManagerService");
Log.d("○△" + this.getClass().getSimpleName(), "WpManagerService(), スレッド名:" + Thread.currentThread().getName());
    }

    // --------------------------------------------------------------------
    //
    // --------------------------------------------------------------------
    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
Log.d("○△" + this.getClass().getSimpleName(), "onStartCommand(), スレッド名:" + Thread.currentThread().getName());

        this.timer = new Timer();
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Intent i = new Intent(ACTION_NAME);
                i.putExtra(KEY_NAME, STATE_START);
                WpManagerService.this.sendBroadcast(i);
            }
        }, 0, 500); //0秒後、500ms秒間隔で実行


        return super.onStartCommand(intent, flags, startId);
    }

    /************************************
     * ここだけ別スレッドで実行される（他はメインスレッドで実行される）
     * 壁紙変更動作を実行→履歴に書き込み
     */
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
Log.d("○△" + this.getClass().getSimpleName(), "onHandleIntent(), スレッド名:" + Thread.currentThread().getName());
        // 別スレッドで実行されているからそのまま壁紙変更&履歴に残す
        WpManager wpManager = new WpManager(this);
        wpManager.execute();
        if ( wpManager.canInsertHistory() ) {
            wpManager.insertHistory();
        } else {
//Log.d("○△" + this.getClass().getSimpleName(), "onHandleIntent()2, スレッド名:" + Thread.currentThread().getName());
//            Intent i = new Intent(ACTION_NAME);
//            i.putExtra(KEY_NAME, STATE_ERROR);
//            this.sendBroadcast(i);
//Log.d("○△" + this.getClass().getSimpleName(), "onHandleIntent()3, スレッド名:" + Thread.currentThread().getName());
        }
//Log.d("○△" + this.getClass().getSimpleName(), "onHandleIntent()4, スレッド名:" + Thread.currentThread().getName());
    }



    @Override
    public void onDestroy() {
        super.onDestroy();

        this.timer.cancel();

        Intent i = new Intent(ACTION_NAME);
        i.putExtra(KEY_NAME, STATE_DESTROY);
        this.sendBroadcast(i);
Log.d("○△" + this.getClass().getSimpleName(), "onDestroy(), スレッド名:" + Thread.currentThread().getName());
    }
}
