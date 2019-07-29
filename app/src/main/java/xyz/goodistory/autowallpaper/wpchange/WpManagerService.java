package xyz.goodistory.autowallpaper.wpchange;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.annotation.Nullable;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import xyz.goodistory.autowallpaper.HistoryModel;

/**
 * 壁紙を変更する用のサービス
 * Created by k-shunsuke on 2018/02/01.
 */
public class WpManagerService extends IntentService {
    // --------------------------------------------------------------------
    //
    // --------------------------------------------------------------------
    // 壁紙変更中にブロードキャストレシーバーへ送信されるアクション
    public static final String ACTION_WPCHANGE_STATE = "xyz.goodistory.autowallpaper.action.WPCHANGE_STATE";
    public static final String EXTRA_WP_STATE = "wp_state";
    public static final int WP_STATE_CHANGING = 1;
    public static final int WP_STATE_DONE = 2;
    public static final int WP_STATE_ERROR = 3;


    // 壁紙をランダムに変更するIntentService用のアクション
    private static final String ACTION_CHANGE_RANDOM = "xyz.goodistory.autowallpaper.action.CHANGE_WP_RANDOM";
    // 指定の壁紙に変更するIntentService用のアクション
    private static final String ACTION_CHANGE_SPECIFIED = "xyz.goodistory.autowallpaper.action.CHANGE_WP_SPECIFIED";



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
    // static メソッド、この IntentService を実行するメソッド
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
     * 指定の画像に壁紙を変更する    IntentServiceを実行
     * @param context コンテキスト
     * @param imgUri 画像の取得元のURI
     * @param sourceKind ディレクトリからかツイッターからか
     * @param intentActionUri クリックしたときの飛ばし先へのIntent
     */
    public static void changeWpSpecified(
            Context context, String imgUri, String sourceKind, @Nullable String intentActionUri,
            @Nullable String deviceImgUri)
    {
        Intent i = new Intent(context, WpManagerService.class);
        i.setAction(ACTION_CHANGE_SPECIFIED);

        i.setData(Uri.parse(imgUri));
        i.putExtra("sourceKind", sourceKind);
        i.putExtra("intentActionUri", intentActionUri);
        i.putExtra("deviceImgUri", deviceImgUri);

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
        // ----------------------------------
        // intentがなければそこで終了
        // ----------------------------------
        if (intent == null) {
            Intent i = new Intent(ACTION_WPCHANGE_STATE);
            i.putExtra(EXTRA_WP_STATE, WP_STATE_ERROR);
            this.sendBroadcast(i);
            return;
        }

        // ----------------------------------
        //
        // ----------------------------------
        final String action = intent.getAction();
        WpManager wpManager = new WpManager(this);
        try {
            if ( ACTION_CHANGE_RANDOM.equals(action) ) {
            // ----------------------------------
            // ランダムで壁紙変更
            // ----------------------------------
                // 別スレッドで実行されているからそのまま壁紙変更&履歴に残す
                wpManager.executeWpRandomSetProcess();
            } else if ( ACTION_CHANGE_SPECIFIED.equals(action) ) {
            // ----------------------------------
            // 指定の壁紙に変更
            // ----------------------------------
                //// intent からデータを取得
                Uri uri = intent.getData();
                String dataUri;
                if (uri == null) {
                    IllegalStateException e = new IllegalStateException("intentのuriが存在しません。");
                    Log.e(e.getClass().getSimpleName(), e.getMessage(), e);
                    throw e;
                } else {
                    dataUri = uri.toString();
                }
                String sourceKind = intent.getStringExtra("sourceKind");
                String intentActionUri = intent.getStringExtra("intentActionUri");
                String deviceImgUri = intent.getStringExtra("deviceImgUri");

                //// 壁紙変更
                ImgGetter imgGetter;
                if ( HistoryModel.SOURCE_KINDS.contains(sourceKind) ) {
                    imgGetter = new ImgGetter(dataUri, intentActionUri, sourceKind, deviceImgUri);
                } else {
                    IllegalStateException e = new IllegalStateException(
                            "intentのsourceKindの値が不正です。");
                    Log.e(e.getClass().getSimpleName(), e.getMessage(), e);
                    throw e;
                }
                wpManager.executeWpSetProcess(imgGetter);
            } else {
                IllegalStateException e = new IllegalStateException("intentのactionの値が不正です。");
                Log.e(e.getClass().getSimpleName(), e.getMessage(), e);
                throw e;
            }
        } catch (Exception e) {
            Intent i = new Intent(ACTION_WPCHANGE_STATE);
            i.putExtra(EXTRA_WP_STATE, WP_STATE_ERROR);
            this.sendBroadcast(i);
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
