package xyz.monogatari.autowallpaper.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
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
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import xyz.monogatari.autowallpaper.MainActivity;
import xyz.monogatari.autowallpaper.NotificationChannelId;
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


    /** 画面がOFFになったときブロードキャストを受信して壁紙を変更するブロードキャストレシーバー */
    private final ScreenOnOffWPChangeBcastReceiver onOffWPChangeReceiver = new ScreenOnOffWPChangeBcastReceiver();
    /** 画面がON/OFFでTimerとAlarmを切り替えるブロードキャストレシーバー */
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
    }

    /************************************
     * サービスが停止時に呼ばれるメソッド
     */
    @Override
    public void onDestroy() {
        super.onDestroy();

        // ----------------------------------
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
        this.isStarted = true;


        // ----------------------------------
        // 通知を作成
        // ----------------------------------
        // ----------
        // 通知チャンネルを作成
        // ----------
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ){ //Android8.0（API 26）以上

            NotificationChannel ntfChannel = new NotificationChannel(
                    NotificationChannelId.RUNNING_MAIN_SERVICE,
                    this.getString(R.string.mainService_notification_ch_name),  //TODO 文言をちゃんとする
                    NotificationManager.IMPORTANCE_LOW
            );

            NotificationManager ntfManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            if (ntfManager != null) {
                ntfManager.createNotificationChannel(ntfChannel);
            }
        }

        // ----------
        // PendingIntentを作成
        // ----------
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                PendingIntentRequestCode.RUNNING_SERVICE,
                new Intent(this, MainActivity.class),
                PendingIntent.FLAG_CANCEL_CURRENT
        );


        // ----------
        // NotificationBuilderを作成してフォアグラウンドでサービス開始＆通知
        // ----------
        //// Notification.Builderを作成する
        NotificationCompat.Builder notifBuilder;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {   //Android8.0（API 26）以上
            notifBuilder = new NotificationCompat.Builder(
                    this,
                    NotificationChannelId.RUNNING_MAIN_SERVICE
            );
        } else {
            //noinspection deprecation
            notifBuilder = new NotificationCompat.Builder(
                    this
            );
        }
        notifBuilder.setContentTitle(this.getString(R.string.mainService_notification_title))
                .setContentText(this.getString(R.string.mainService_notification_text))
                .setSmallIcon(R.drawable.ic_notification_running_service)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(pendingIntent)
                //ロック画面に通知表示しない（注意、ここの設定は端末の設定で上書きされる）
                .setVisibility(NotificationCompat.VISIBILITY_SECRET);

        //// フォアグラウンドでサービスを開始＆通知
        Notification notification = notifBuilder.build();
        this.startForeground(NotifyId.RUNNING_SERVICE, notification);

        // ----------------------------------
        //
        // ----------------------------------
        //// 通常の場合
        if ( this.sp.getBoolean(SettingsFragment.KEY_WHEN_SCREEN_ON, false) ) {
            this.setScreenOnListener();
        }
        if ( this.sp.getBoolean(SettingsFragment.KEY_WHEN_TIMER, false) ) {
            this.setTimerListener();
        }

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
        return this.binder;
    }

    /************************************
     * 設定画面の値が変更されたときに呼ばれるコールバック（自作）
     * @param key SharedPreferenceのキー名
     */
    public void onSPChanged(String key) {
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
            case SettingsFragment.KEY_WHEN_TIMER_START_TIMING_1:
            case SettingsFragment.KEY_WHEN_TIMER_INTERVAL:
                if ( this.sp.getBoolean(SettingsFragment.KEY_WHEN_TIMER, false) ) {
                    this.unsetTimerListener();
                    this.setTimerListener();
                }
                break;
        }
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
        this.registerReceiver(this.onOffWPChangeReceiver, intentFilter);
    }
    /************************************
     * 画面ON時壁紙変更のイベントリスナー削除
     */
    private void unsetScreenOnListener() {
        this.unregisterReceiver(this.onOffWPChangeReceiver);
    }

    /************************************
     * 設定タイマー壁紙変更のイベントリスナー登録
     */
    private void setTimerListener() {
        // ----------------------------------
        // タイマーセット
        // ----------------------------------
        this.setTimer();

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
     *
     * @return unsetできたらtrue、元々 unset状態でunsetする必要なかったらfalse
     */
    @SuppressWarnings("UnusedReturnValue")
    private boolean unsetTimerListener() {
        // ----------
        // 時間で壁紙セットするタイマー
        // ----------
        boolean canCancelTimer = this.cancelTimer();

        // ----------
        // 画面OFF or ONになったとき Timer→Alarm or Alarm→Timer にするブロードキャストレシーバーの処理
        // ----------
        boolean canCancelReceiver;  //return用
        if (this.timerReceiver == null) {
            canCancelReceiver = false;
        } else {
            this.unregisterReceiver(this.timerReceiver);
            this.timerReceiver = null;
            canCancelReceiver = true;
        }

        return canCancelTimer && canCancelReceiver;
    }

    /************************************
     * 次のタイマーが実行されるdelayのミリ秒を求める
     * 未来の設定時刻でもOK
     * @param startUnixTimeMsec 設定された開始基準時間、UNIXタイム形式
     * @param periodMsec periodMsec間隔で壁紙交換が実行される
     * @param nowUnixTimeMsec 現在の時刻、UNIXタイム形式
     * @return 計算後のUNIXタイム
     */
    public static long calcDelayMsec(long startUnixTimeMsec, long periodMsec, long nowUnixTimeMsec) {
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
                SettingsFragment.KEY_WHEN_TIMER_INTERVAL,
                this.getString(R.string.setting_when_timer_interval_values_default)
        ));

        final long startTimeUnixTime = this.sp.getLong(
                SettingsFragment.KEY_WHEN_TIMER_START_TIMING_1, System.currentTimeMillis()
        );
        final long delayMsec = calcDelayMsec(startTimeUnixTime, intervalMsec, System.currentTimeMillis());


        // ----------
        // 本番
        // ----------
        this.timer = new Timer();
        // schedule() は実行が遅延したらその後も遅延する、
        // （例）1分間隔のタイマーが10秒遅れで実行されると次のタイマーは1分後に実行される
        // scheduleAtFixedRate() は実行が遅延したら遅延を取り戻そうと実行する、
        // （例）1分間隔のタイマーが10秒遅れで実行されると次のタイマーは50秒後に実行される
        this.timer.scheduleAtFixedRate(
                new TimerTask() {
                    @Override
                    public void run() {

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
                this.sp.getLong(SettingsFragment.KEY_WHEN_TIMER_START_TIMING_1, System.currentTimeMillis()),
                Long.parseLong( this.sp.getString(
                                SettingsFragment.KEY_WHEN_TIMER_INTERVAL,
                                this.getString(R.string.setting_when_timer_interval_values_default))),
                nowUnixTimeMsec
        );
        long wakeUpUnixTime = delayMsec + nowUnixTimeMsec;


        // ----------------------------------
        // 本番
        // ----------------------------------
        try {
            if (Build.VERSION.SDK_INT <= 18) {   // ～Android 4.3
                this.alarmManager.set(AlarmManager.RTC_WAKEUP, wakeUpUnixTime, this.pendingIntent);

            } else if ( Build.VERSION.SDK_INT <= 22) { // Android4.4(KitKat) ～ Android 5.1(Lollipop)
                this.alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP, wakeUpUnixTime, this.pendingIntent);

            } else {  // Android 6.0(Marshmallow)～
                this.alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, wakeUpUnixTime, this.pendingIntent);

            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }


    /************************************
     * これはブロードキャストレシーバーからも呼ばれてるから敢えて外に外している
     */
    public boolean cancelTimer() {
        if (this.timer == null) {   // 元々タイマーがセットされていないときは何もしない
            return false;
        } else {
            this.timer.cancel();
            return true;
        }
    }
    /************************************
     *
     */
    public void cancelAlarm() {
        this.alarmManager.cancel(this.pendingIntent);
    }
    


}
