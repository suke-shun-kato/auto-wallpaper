package xyz.monogatari.autowallpaper.wpchange;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
    // TODO これブロードギャスとレシーバーの方に持っていきたい
    public static final String ACTION_NAME = "xyz.monogatari.autowallpaper.WP_SERVICE_ACTION";

    // 壁紙をランダムに変更するIntentService用のアクション
    public static final String ACTION_CHANGE_RANDAM = "xyz.monogatari.autowallpaper.action.CHANGE_WP_RANDAM";
    // 指定の壁紙に変更するIntentService用のアクション
    public static final String ACTION_CHANGE_SPECIFIED = "xyz.monogatari.autowallpaper.action.CHANGE_WP_SPECIFIED";

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

    /**
     * ランダムに壁紙を変更するIntentServiceを実行
     * @param context
     */
    public static void changeWpRandam(Context context) {
        Intent i = new Intent(context, WpManagerService.class);
        i.setAction(ACTION_CHANGE_RANDAM);
        context.startService(i);
    }

    /**
     * 指定の画像に壁紙を変更するIntentServiceを実行
     * @param context
     * @param imgUri
     * @param sourceKind
     * @param intentActionUri
     */
    public static void changeWpSpecified(Context context, String imgUri, String sourceKind, String intentActionUri) {
        Intent i = new Intent(context, WpManagerService.class);
        i.setAction(ACTION_CHANGE_SPECIFIED);

        i.setData(Uri.parse(imgUri));
        i.putExtra("sourceKind", sourceKind);
        i.putExtra("intentActionUri", intentActionUri);

        context.startService(i);
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
        if (intent != null) {
            final String action = intent.getAction();
            WpManager wpManager = new WpManager(this);

            if ( ACTION_CHANGE_RANDAM.equals(action) ) {
            // ----------------------------------
            // ランダムで壁紙変更
            // ----------------------------------
                // 別スレッドで実行されているからそのまま壁紙変更&履歴に残す
                boolean canExe = wpManager.executeWpSetRandamTransaction();

                if ( !canExe ) {
                    Intent i = new Intent(ACTION_NAME);
                    i.putExtra(KEY_NAME, STATE_ERROR);
                    this.sendBroadcast(i);
                }

            } else if ( ACTION_CHANGE_SPECIFIED.equals(action) ) {
            // ----------------------------------
            // 指定の壁紙に変更
            // ----------------------------------
                //// intent からデータを取得
                Uri dataUri = intent.getData();
                String sourceKind = intent.getStringExtra("sourceKind");
                String intentActionUri = intent.getStringExtra("intentActionUri");

                //// 壁紙変更
                ImgGetter imgGetter;
                switch (sourceKind) {
                    case "ImgGetterDir":
                        imgGetter = new ImgGetterDir(dataUri.toString(), intentActionUri);
                        break;
                    case "ImgGetterTw":
                        imgGetter = new ImgGetterTw(dataUri.toString(), intentActionUri);
                        break;
                    default:
                        throw new RuntimeException("histories.source_kindの値が不正です。");

                }
                wpManager.executeWpSetTransaction(imgGetter);
            } else {
                throw new RuntimeException("intentのactionの値が不正です。");
            }
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
