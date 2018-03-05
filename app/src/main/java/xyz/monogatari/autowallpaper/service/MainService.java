package xyz.monogatari.autowallpaper.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import xyz.monogatari.autowallpaper.MainActivity;
import xyz.monogatari.autowallpaper.NotifyId;
import xyz.monogatari.autowallpaper.PendingIntentRequestCode;
import xyz.monogatari.autowallpaper.R;
import xyz.monogatari.autowallpaper.SettingsFragment;
import xyz.monogatari.autowallpaper.wpchange.WpManagerService;

/**
 * Created by k-shunsuke on 2017/12/12.
 * 裏で壁紙を変更するサービス
 */
public class MainService extends Service {
    // --------------------------------------------------------------------
    // フィールド、Util
    // --------------------------------------------------------------------
    /** 通常の開始されたサービスが実行中か？ */
    private boolean isStarted = false;

    /** ブロードキャストレシーバーのインスタンス */
    private final ScreenOnOffBcastReceiver onOffReceiver = new ScreenOnOffBcastReceiver();
    private TimerBcastReceiver timerReceiver;

    /** SharedPreference */
    private SharedPreferences sp;

    /** 時間設定用のタイマー、タイマーは一度cancel()したら再度schedule()できないのでここでnewしない */
    private Timer timer;
    private AlarmManager alarmManager;

    private PendingIntent pendingIntent;

    // --------------------------------------------------------------------
    // フィールド（バインド用）
    // --------------------------------------------------------------------
    /**
     * バインド開始時に返すオブジェクト
     */
    private final IBinder binder = new MainService.MainServiceBinder();

    /**
     * ここは匿名クラスでは宣言していはいけない、バインドした側のコールバックでこのクラスが使われているから
     * BinderはIBinderインターフェースをimplementしている
     */
    public class MainServiceBinder extends Binder {
        /**
         * バインド先からこのサービスを取得（バインド先から自由にサービスのメソッドを使えるようにするため）
         * @return このクラスのインスタンス
         */
        public MainService getService() {
            // 自分のサービスを返す
            return MainService.this;

        }
    }

    // --------------------------------------------------------------------
    // メソッド（通常、バインド両方）
    // --------------------------------------------------------------------
    /************************************
     * サービス開始時、たった1回だけ実行されるメソッド
     */
    @Override
    public void onCreate() {
        super.onCreate();
        this.sp = PreferenceManager.getDefaultSharedPreferences(this);


        Notification.Builder builder = new Notification.Builder(this)
                .setContentTitle(this.getString(R.string.mainService_notification_title))
                .setContentText(this.getString(R.string.mainService_notification_text))
                .setSmallIcon(R.drawable.ic_notification_running_service)
                .setWhen(System.currentTimeMillis())

                .setContentIntent(
                        PendingIntent.getActivity(
                                this,
                                PendingIntentRequestCode.RUNNING_SERVICE,
                                new Intent(this, MainActivity.class),
                                PendingIntent.FLAG_CANCEL_CURRENT
                        )
                );

        if (Build.VERSION.SDK_INT >= 21) {
            //APIレベル21以上の場合, Android5.0以上のとき
            //ロック画面に通知表示しない（注意、ここの設定は端末の設定で上書きされる）
            builder = builder.setVisibility(Notification.VISIBILITY_SECRET);
        }


        this.startForeground(NotifyId.RUNNING_SERVICE, builder.build());

Log.d("○"+this.getClass().getSimpleName(), "onCreate()が呼ばれた hashCode: " + this.hashCode());
    }

    /************************************
     * サービスが停止時に呼ばれるメソッド
     */
    @Override
    public void onDestroy() {
Log.d("○"+this.getClass().getSimpleName(), "onDestroy()が呼ばれた hashCode: " + this.hashCode());
        super.onDestroy();

        // ------------------------                                                                 ----------
        // 途中で切り上げ
        // ----------------------------------
        if ( !this.isStarted ) {
            return;
        }

        // ----------------------------------
        //
        // ----------------------------------
        if ( this.sp.getBoolean(SettingsFragment.KEY_WHEN_SCREEN_ON, false) ) {
            this.unsetScreenOnListener();
        }
        if (this.sp.getBoolean(SettingsFragment.KEY_WHEN_TIMER, false)) {
            this.unsetTimerListener();
        }

        this.isStarted = false;

    }
    // --------------------------------------------------------------------
    // メソッド（開始されたサービス（通常サービス）のとき）
    // --------------------------------------------------------------------
    /************************************
     * 通常のサービスを開始したとき、現状では以下の場合が考えられる
     * 「メインActivityで開始ボタンを押したとき」
     * 「アクティビティを廃棄したときの再起動」
     * 「アラームでセットした時間の壁紙変更」
     * @param intent startService() でサービスで送ったIntent
     * @param flags 追加データ、0 か START_FLAG_REDELIVERY か START_FLAG_RETRY
     * @param startId ユニークなID, startService()を重複して実行するたびに ++startId される
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
String action = null;
if (intent != null) {
    action = intent.getAction();
}
Log.d("○"+this.getClass().getSimpleName(), "onStartCommand(): hashCode: " + this.hashCode() + ", intent: " + intent + ",action: " + action + ", flags: "+flags + ", startId: "+ startId);


        this.isStarted = true;

        // ----------------------------------
        //
        // ----------------------------------
//
//        //// アラームからスタートした場合
//        if (intent != null && intent.getAction() != null && intent.getAction().equals(ACTION_WALLPAPER_CHANGE)) {
//            if ( this.sp.getBoolean(SettingsFragment.KEY_WHEN_TIMER, false) ) {
//Log.d("○"+getClass().getSimpleName(), "onStartCommand(): Alarm");
//                new WpManager(this).executeNewThread();
//            }
//        } else {
        //// 通常の場合
            if ( this.sp.getBoolean(SettingsFragment.KEY_WHEN_SCREEN_ON, false) ) {
                this.setScreenOnListener();
            }
            if ( this.sp.getBoolean(SettingsFragment.KEY_WHEN_TIMER, false) ) {
                this.persistStart0();
                this.setTimerListener();
            }
//        }

        return START_STICKY;
    }


    // --------------------------------------------------------------------
    // メソッド（バインドサービス関連）
    // --------------------------------------------------------------------
    /************************************
     * バインドでサービス開始したとき使用、設定画面が表示されたとき
     * @param intent bind
     * @return Binderインターフェースを実装したもの
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
Log.d("○"+this.getClass().getSimpleName(), "onBind()が呼ばれた  hashCode: " + this.hashCode());
        return this.binder;
    }

    /************************************
     * バインドが終わった時、設定画面の表示が終わる時
     * @param intent バインド時に貰ったインテントとおなじの
     */
    @Override
    public boolean onUnbind(Intent intent) {
Log.d("○"+this.getClass().getSimpleName(), "onUnbind()が呼ばれた hashCode: " + this.hashCode());

        return super.onUnbind(intent);
    }

    /************************************
     *
     */
    @Override
    public void onRebind(Intent intent) {
Log.d("○"+this.getClass().getSimpleName(), "onRebind()が呼ばれた hashCode: " + this.hashCode());
        super.onRebind(intent);
    }

    /************************************
     * 設定画面の値が変更されたときに呼ばれるコールバック（自作）
     * @param key SharedPreferenceのキー名
     */
    public void onSPChanged(String key) {
Log.d("○"+this.getClass().getSimpleName(), "onSPChanged()が呼ばれた hashCode: " + this.hashCode());
Log.d("○"+this.getClass().getSimpleName(), "key名: " + key);

        // ----------------------------------
        // 例外処理
        // ----------------------------------
        if (!this.isStarted) {
            //開始されたサービス（通常サービス）が起動中でないときは途中で切り上げ
            return;
        }

        // ----------------------------------
        // 通常処理
        // ----------------------------------
        switch (key) {
            // ----------------------------------
            // from
            // ----------------------------------
            //// fromは壁紙セット時に判定するのでここの記述は不要

            // ----------------------------------
            // When
            // ----------------------------------
            case SettingsFragment.KEY_WHEN_SCREEN_ON:
                // 電源ON設定がONのとき設定
                if ( this.sp.getBoolean(SettingsFragment.KEY_WHEN_SCREEN_ON, false) ) {
                    this.setScreenOnListener();
                } else {
                    this.unsetScreenOnListener();
                }
                break;
            case SettingsFragment.KEY_WHEN_TIMER:
                // 時間設定
                if ( this.sp.getBoolean(SettingsFragment.KEY_WHEN_TIMER, false) ) {
                    this.setTimerListener();
                } else {
                    this.unsetTimerListener();
                }
                break;
            case SettingsFragment.KEY_WHEN_TIMER_START_TIMING_0:
                if ( this.sp.getBoolean(SettingsFragment.KEY_WHEN_TIMER, false) ) {
Log.d("○"+getClass().getSimpleName(), "WHEN_TIMER_START_TIMING_0:" + this.sp.getLong(SettingsFragment.KEY_WHEN_TIMER_START_TIMING_0, 0L)+", NOW:"+System.currentTimeMillis() );
                    this.unsetTimerListener();
                    this.setTimerListener();
                }
                break;
            case SettingsFragment.KEY_WHEN_TIMER_INTERVAL:
            case SettingsFragment.KEY_WHEN_TIMER_START_TIMING_1:
                if ( this.sp.getBoolean(SettingsFragment.KEY_WHEN_TIMER, false) ) {
                    // ここで_0を変更するとまたonSPChanged()が叩かれる
                    this.persistStart0();
                }
                break;
        }
    }

    /************************************
     * START_TIMING_1 とINTERVALからSTART_TIMING_0 に値を永続化（保存）する
     * ※注意、ここに値を保存すると、サービス開始していて設定画面表示中だと onSPChanged() が発火します
     */
    private void persistStart0() {
        double mag = Double.parseDouble(this.sp.getString(SettingsFragment.KEY_WHEN_TIMER_START_TIMING_1, "0.0"));
        long intervalMsec = Long.parseLong(this.sp.getString(SettingsFragment.KEY_WHEN_TIMER_INTERVAL, "5000"));
        long nowUnixTimeMsec = System.currentTimeMillis();
Log.d("○"+getClass().getSimpleName(), "persistStart0(): mag:"+mag+", intervalMsec:"+intervalMsec+", nowUnixTimeMsec:"+nowUnixTimeMsec);

        this.sp.edit().putLong(
                SettingsFragment.KEY_WHEN_TIMER_START_TIMING_0,
                Math.round( calcStartUnixTime(intervalMsec, mag, nowUnixTimeMsec ) )  //開始時間のUnixタイム[ms]
        ).apply();
    }

    /************************************
     * Timerの開始時刻のUnixTime[ms] を求める
     * @param intervalMsec Timerの間隔
     * @param mag a
     * @param nowUnixTimeMsec 現在のUNIXタイム
     * @return Timerの開始時刻のUnixTime[ms]
     */
    private static double calcStartUnixTime(long intervalMsec, double mag, long nowUnixTimeMsec) {
        // 境界値辺りの値の修正はば[ms]
        final double  COMPRESS_MSEC = 500.0;
        
        // タイマーをセットするときには少し秒数が経過しているので、500ms秒をここで追加
        // ----------------------------------
        // 例外処理
        // ----------------------------------
        if (intervalMsec < COMPRESS_MSEC * 2) {
            throw new RuntimeException("intervalMsecが大きすぎます");
        }
        
        // ----------------------------------
        // 本番
        // ----------------------------------
        //// メイン処理
        double xxxMSecondsAfter = mag * intervalMsec;

        //// 境界値付近（0[ms]付近とintervalMsec[ms]付近）は実際にTimerセットするときずれるので内側に値を修正
        if ( 0.0 <= xxxMSecondsAfter && xxxMSecondsAfter < COMPRESS_MSEC) {
            xxxMSecondsAfter = COMPRESS_MSEC;
        }
        if ( intervalMsec - COMPRESS_MSEC < xxxMSecondsAfter && xxxMSecondsAfter <= intervalMsec ) {
            xxxMSecondsAfter = intervalMsec - COMPRESS_MSEC;
        }

        return xxxMSecondsAfter + nowUnixTimeMsec;
    }

    // --------------------------------------------------------------------
    // 自作リスナー登録
    // --------------------------------------------------------------------
    /************************************
     * 画面ON時壁紙変更のイベントリスナーを登録
     */
    private void setScreenOnListener() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        this.registerReceiver(this.onOffReceiver, intentFilter);
    }
    /************************************
     * 画面ON時壁紙変更のイベントリスナー削除
     */
    private void unsetScreenOnListener() {
Log.d("○"+ getClass().getSimpleName(), "unsetScreenOnListener(): "+this.onOffReceiver);
        this.unregisterReceiver(this.onOffReceiver);
    }

    /************************************
     * 設定タイマー壁紙変更のイベントリスナー登録
     */
    private void setTimerListener() {
        // ----------------------------------
        // タイマーセット
        // ----------------------------------
        this.setTimer();
Log.d("○"+getClass().getSimpleName(), "setTimerListener()");

        // ----------------------------------
        // ブロードキャストレシーバーを設置
        // ----------------------------------
        this.timerReceiver = new TimerBcastReceiver();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        this.registerReceiver(this.timerReceiver, intentFilter);

    }
    /************************************
     * 設定タイマー時壁紙変更のイベントリスナー削除
     */
    private void unsetTimerListener() {
Log.d("○"+this.getClass().getSimpleName(), "unsetTimerListener(), hashCode()="+this.timerReceiver.hashCode());
        this.cancelTimer();
        this.unregisterReceiver(this.timerReceiver);
    }

    /************************************
     * 次のタイマーが実行されるdelayのミリ秒を求める
     * 未来の設定時刻でもOK
     * @param startUnixTimeMsec 設定された開始時間、UNIXタイム形式
     * @param periodMsec periodMsec間隔で壁紙交換が実行される
     * @param nowUnixTimeMsec 現在のUNIXタイム形式
     * @return 計算後のUNIXタイム
     */
    public static long calcDelayMsec(long startUnixTimeMsec, long periodMsec, long nowUnixTimeMsec) {
        // ----------------------------------
        // 例外処理
        // ----------------------------------
//        if (startUnixTimeMsec == -1L) {
//            return 0;
//        }
        // ----------------------------------
        // 通常処理
        // ----------------------------------
        // xxxは整数
        // startUnixTimeMsec + (xxx-1) * periodMsec < nowUnixTimeMsec < startUnixTimeMsec + xxx * periodMsec
        // (xxx-1) * periodMsec < nowUnixTimeMsec - startUnixTimeMsec < xxx * periodMsec
        // xxx-1 < (nowUnixTimeMsec - startUnixTimeMsec)/periodMsec < xxx

        long xxx = (long)Math.ceil( (double)(nowUnixTimeMsec - startUnixTimeMsec)/periodMsec );

        return startUnixTimeMsec + xxx * periodMsec - nowUnixTimeMsec;
    }
    
    /************************************
     * これはブロードキャストレシーバーからも呼ばれているので敢えてpublicでsetTimerListener()の外に外している
     */
    public void setTimer() {

        // ----------
        // 変数準備
        // ----------
        final long intervalMsec = Long.parseLong(this.sp.getString(
                SettingsFragment.KEY_WHEN_TIMER_INTERVAL, ""
        ));
        final long startTimeUnixTime = this.sp.getLong(
                SettingsFragment.KEY_WHEN_TIMER_START_TIMING_0, System.currentTimeMillis()
        );
        final long delayMsec = calcDelayMsec(startTimeUnixTime, intervalMsec, System.currentTimeMillis());
Log.d("○"+getClass().getSimpleName(), "setTimer()______________: intervalMsec: "+intervalMsec + ", startTimeUnixTime: " + startTimeUnixTime + ", delayMsec: " + delayMsec);

        // ----------
        // 本番
        // ----------
        this.timer = new Timer();
        // schedule は実行が遅延したらその後も遅延する、
        // （例）1分間隔のタイマーが10秒遅れで実行されると次のタイマーは1分後に実行される
        // scheduleAtFixedRate は実行が遅延したら遅延を取り戻そうと実行する、
        // （例）1分間隔のタイマーが10秒遅れで実行されると次のタイマーは50秒後に実行される
        this.timer.scheduleAtFixedRate(
                new TimerTask() {
                    @Override
                    public void run() {
Log.d("○" + getClass().getSimpleName(), "setTimer(): TimerTask.run(): delay:"+delayMsec/1000+"秒 period:"+intervalMsec/1000+"秒, hash: " + this.hashCode());
//                        new WpManager(MainService.this).executeNewThread();

                        Intent i = new Intent(MainService.this, WpManagerService.class);
                        startService(i);
                    }
                },
                delayMsec,
                intervalMsec
        );
    }
    
    /************************************
     * 電源OFF時のタイマーのアラーム起動
     */
    public void setAlarm() {
        // ----------------------------------
        // 準備
        // ----------------------------------
        //// alarmManager
        this.alarmManager = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);

        //// pendingIntent
        this.pendingIntent = PendingIntent.getService(
                this,
                //呼び出し元を識別するためのコード
                PendingIntentRequestCode.TIMER_ALARM,
                new Intent(this, WpManagerService.class),
                //PendingIntentの挙動を決めるためのflag、複数回送る場合一番初めに生成したものだけ有効になる
                PendingIntent.FLAG_ONE_SHOT
        );

        //// wakeUpUnixTime アラームの起動時間
        long nowUnixTimeMsec = System.currentTimeMillis();
        long delayMsec = calcDelayMsec(
                this.sp.getLong(SettingsFragment.KEY_WHEN_TIMER_START_TIMING_0, System.currentTimeMillis()),
                Long.parseLong(this.sp.getString(SettingsFragment.KEY_WHEN_TIMER_INTERVAL, "5000")),
                nowUnixTimeMsec
        );
        long wakeUpUnixTime = delayMsec + nowUnixTimeMsec;

Log.d("○"+getClass().getSimpleName(), "setAlarm(), delayMsec=" + delayMsec + "ミリ秒, nowUnixTimeMsec=" + nowUnixTimeMsec + "ミリ秒, wakeUpUnixTime=" + wakeUpUnixTime + "ミリ秒");

        // ----------------------------------
        // 本番
        // ----------------------------------
        try {
            if (Build.VERSION.SDK_INT <= 18) {   // ～Android 4.3
                this.alarmManager.set(AlarmManager.RTC_WAKEUP, wakeUpUnixTime, this.pendingIntent);

            } else if (19 <= Build.VERSION.SDK_INT && Build.VERSION.SDK_INT <= 22) {// Android4.4～Android 5.1
                this.alarmManager.setExact(AlarmManager.RTC_WAKEUP, wakeUpUnixTime, this.pendingIntent);

            } else if (23 <= Build.VERSION.SDK_INT ) {  // Android 6.0～
//Log.d("○","通ってますよa！！！！！！！！！！！！");
                this.alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, wakeUpUnixTime, this.pendingIntent);

            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }


    /************************************
     * これはブロードキャストレシーバーからも呼ばれてるから敢えて外に外している
     */
    public void cancelTimer() {
Log.d("○"+this.getClass().getSimpleName(), "cancelTimer()_");
        this.timer.cancel();
    }
    /************************************
     *
     */
    public void cancelAlarm() {
Log.d("○"+getClass().getSimpleName(), "cancelAlarm()_");
        this.alarmManager.cancel(this.pendingIntent);
    }
    


}