package xyz.monogatari.autowallpaper.wpchange;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

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
    public static final String ACTION_NAME = "xyz.monogatari.autowallpaper.WP_SERVICE_ACTION";
    public static final String KEY_NAME = "state";
    public static final int STATE_ON = 1;
    public static final int STATE_DESTROY = 2;
    public static final int STATE_ERROR = 3;


    private Timer timer;

    // --------------------------------------------------------------------
    // 
    // --------------------------------------------------------------------
    public WpManagerService(String name) {
        super(name);
    }

    public WpManagerService() {
        super("WpManagerService");
    }

    // --------------------------------------------------------------------
    //
    // --------------------------------------------------------------------
    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        if (this.timer == null) {   //連続でこのサービスが走ったらonDestroy()でタイマーがcancelされる前にインスタンスが上書きされるから、最初のTimerがcancelされないのでその対策
            this.timer = new Timer();
            this.timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Intent i = new Intent(ACTION_NAME);
                    i.putExtra(KEY_NAME, STATE_ON);
                    WpManagerService.this.sendBroadcast(i);
                }
            }, 0, 500); //0秒後、500ms秒間隔で実行
        }

        return super.onStartCommand(intent, flags, startId);
    }

    /************************************
     * ここだけ別スレッドで実行される（他はメインスレッドで実行される）
     * 壁紙変更動作を実行→履歴に書き込み
     */
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        // 別スレッドで実行されているからそのまま壁紙変更&履歴に残す
        WpManager wpManager = new WpManager(this);
        boolean canExe = wpManager.execute();
        if ( !canExe ) {
            Intent i = new Intent(ACTION_NAME);
            i.putExtra(KEY_NAME, STATE_ERROR);
            this.sendBroadcast(i);
        }
    }



    @Override
    public void onDestroy() {
        super.onDestroy();

        this.timer.cancel();

        Intent i = new Intent(ACTION_NAME);
        i.putExtra(KEY_NAME, STATE_DESTROY);
        this.sendBroadcast(i);
    }
}
