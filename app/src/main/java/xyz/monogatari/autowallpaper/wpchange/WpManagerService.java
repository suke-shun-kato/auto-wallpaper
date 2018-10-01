package xyz.monogatari.autowallpaper.wpchange;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;

import java.util.Timer;
import java.util.TimerTask;

import xyz.monogatari.autowallpaper.HistoryModel;

/**
 * 壁紙を変更する用のサービス
 * Created by k-shunsuke on 2018/02/01.
 */
public class WpManagerService extends IntentService {
    // --------------------------------------------------------------------
    //
    // --------------------------------------------------------------------
    // 壁紙変更中にブロードキャストレシーバーへ送信されるアクション
    public static final String ACTION_WPCHANGE_STATE = "xyz.monogatari.autowallpaper.action.WPCHANGE_STATE";
    public static final String EXTRA_WP_STATE = "wp_state";
    public static final int WP_STATE_CHANGING = 1;
    public static final int WP_STATE_DONE = 2;
    public static final int WP_STATE_ERROR = 3;


    // 壁紙をランダムに変更するIntentService用のアクション
    private static final String ACTION_CHANGE_RANDOM = "xyz.monogatari.autowallpaper.action.CHANGE_WP_RANDOM";
    // 指定の壁紙に変更するIntentService用のアクション
    private static final String ACTION_CHANGE_SPECIFIED = "xyz.monogatari.autowallpaper.action.CHANGE_WP_SPECIFIED";



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
     * @param context コンテキスト
     */
    public static void changeWpRandom(Context context) {
        Intent i = new Intent(context, WpManagerService.class);
        i.setAction(ACTION_CHANGE_RANDOM);
        context.startService(i);
    }

    /**
     * 指定の画像に壁紙を変更するIntentServiceを実行
     * @param context コンテキスト
     * @param imgUri 画像の取得元のURI
     * @param sourceKind ディレクトリからかツイッターからか
     * @param intentActionUri クリックしたときの飛ばし先へのIntent
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
        if (this.timer == null) {   // タイマー実行中のときはタイマーセット処理を飛ばす
            this.timer = new Timer();
            this.timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Intent i = new Intent(ACTION_WPCHANGE_STATE);
                    i.putExtra(EXTRA_WP_STATE, WP_STATE_CHANGING);
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

            boolean canExe;

            if ( ACTION_CHANGE_RANDOM.equals(action) ) {
            // ----------------------------------
            // ランダムで壁紙変更
            // ----------------------------------
                // 別スレッドで実行されているからそのまま壁紙変更&履歴に残す
                canExe = wpManager.executeWpSetRandomTransaction();

            } else if ( ACTION_CHANGE_SPECIFIED.equals(action) ) {
            // ----------------------------------
            // 指定の壁紙に変更
            // ----------------------------------
                //// intent からデータを取得
                String dataUri;
                try {
                    dataUri = intent.getData().toString();
                } catch (Exception e) {
                    throw new RuntimeException("uriの値が不正です。");
                }
                String sourceKind = intent.getStringExtra("sourceKind");
                String intentActionUri = intent.getStringExtra("intentActionUri");

                //// 壁紙変更
                ImgGetter imgGetter;
                switch (sourceKind) {
                    case HistoryModel.SOURCE_DIR:
                        imgGetter = new ImgGetterDir(dataUri, intentActionUri);
                        break;
                    case HistoryModel.SOURCE_TW:
                        imgGetter = new ImgGetterTw(dataUri, intentActionUri);
                        break;
                    default:
                        throw new RuntimeException("histories.source_kindの値が不正です。");

                }
                canExe = wpManager.executeWpSetTransaction(imgGetter);
            } else {
                throw new RuntimeException("intentのactionの値が不正です。");
            }

            // 壁紙変更がうまく行かなかったらブロードキャストでエラーを送信
            if ( !canExe ) {
                Intent i = new Intent(ACTION_WPCHANGE_STATE);
                i.putExtra(EXTRA_WP_STATE, WP_STATE_ERROR);
                this.sendBroadcast(i);
            }
        }

    }



    @Override
    public void onDestroy() {
        super.onDestroy();

        if (this.timer != null) {
            this.timer.cancel();
        }
        this.timer = null;

        Intent i = new Intent(ACTION_WPCHANGE_STATE);
        i.putExtra(EXTRA_WP_STATE, WP_STATE_DONE);
        this.sendBroadcast(i);
    }
}
