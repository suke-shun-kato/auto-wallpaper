package xyz.goodistory.autowallpaper.service;

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

import xyz.goodistory.autowallpaper.TimerWpChangeReceiver;
import xyz.goodistory.autowallpaper.MainActivity;
import xyz.goodistory.autowallpaper.PendingIntentRequestCode;
import xyz.goodistory.autowallpaper.R;

/**
 * 裏で壁紙を変更するサービス
 */
@SuppressWarnings("unused")
public class MainService extends Service {
    // --------------------------------------------------------------------
    // フィールド、Util
    // --------------------------------------------------------------------
    /** 通常の開始されたサービスが実行中か？ */
    private boolean mIsStarted = false;

    /** 画面がOFFになったときブロードキャストを受信して壁紙を変更するブロードキャストレシーバー */
    private final ScreenOnOffWPChangeBcastReceiver mOnOffWPChangeReceiver = new ScreenOnOffWPChangeBcastReceiver();

    /** SharedPreference */
    private SharedPreferences mSp;

    /** 指定時間に壁紙がランダムチェンジする */
    PendingIntent mWpChangePdIntent;

    //// preference key
    private String PREFERENCE_KEY_WHEN_SCREEN_ON;
    private String PREFERENCE_KEY_WHEN_TIMER_CALLS;
    private String PREFERENCE_KEY_START_TIME;
    private String PREFERENCE_KEY_TIMER_INTERVAL;

    // --------------------------------------------------------------------
    // フィールド（バインド用）
    // --------------------------------------------------------------------
    /**
     * バインド開始時に返すオブジェクト
     */
    private final IBinder mBinder = new MainService.MainServiceBinder();

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

        mSp = PreferenceManager.getDefaultSharedPreferences(this);

        //// preference key
        PREFERENCE_KEY_WHEN_SCREEN_ON = getString(R.string.preference_key_when_screen_on);
        PREFERENCE_KEY_WHEN_TIMER_CALLS =  getString(R.string.preference_key_when_timer_calls);
        PREFERENCE_KEY_START_TIME =  getString(R.string.preference_key_start_time);
        PREFERENCE_KEY_TIMER_INTERVAL =  getString(R.string.preference_key_timer_interval);

        mWpChangePdIntent = PendingIntent.getBroadcast(
                this, TimerWpChangeReceiver.REQUEST_CODE_MAIN_SERVICE,
                new Intent(this, TimerWpChangeReceiver.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
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
        if ( !this.mIsStarted) {
            return;
        }

        // ----------------------------------
        //
        // ----------------------------------
        if ( mSp.getBoolean(PREFERENCE_KEY_WHEN_SCREEN_ON, false) ) {
            this.unsetScreenOnListener();
        }
        if (mSp.getBoolean(PREFERENCE_KEY_WHEN_TIMER_CALLS, false)) {
            this.unsetTimerListener();
        }

        this.mIsStarted = false;

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
    @SuppressWarnings("SameReturnValue")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.mIsStarted = true;



        String notificationChannelId = getResources().getString(
                R.string.id_notificationChannel_runningMainService);
        // ----------------------------------
        // 通知を作成
        // ----------------------------------
        // ----------
        // 通知チャンネルを作成
        // ----------
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ){ //Android8.0（API 26）以上

            NotificationChannel ntfChannel = new NotificationChannel(
                    notificationChannelId,
                    getString(R.string.mainService_notification_ch_name),
                    NotificationManager.IMPORTANCE_LOW
            );

            NotificationManager ntfManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (ntfManager != null) {
                ntfManager.createNotificationChannel(ntfChannel);
            }
        }

        // ----------
        // PendingIntentを作成
        // ----------
        PendingIntent foregroundPdIntent = PendingIntent.getActivity(
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
            notifBuilder = new NotificationCompat.Builder(this, notificationChannelId);
        } else {
            //noinspection deprecation
            notifBuilder = new NotificationCompat.Builder(this);
        }
        notifBuilder.setContentTitle(this.getString(R.string.mainService_notification_title))
                .setContentText(this.getString(R.string.mainService_notification_text))
                .setSmallIcon(R.drawable.ic_notification_running_service)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(foregroundPdIntent)
                //ロック画面に通知表示しない（注意、ここの設定は端末の設定で上書きされる）
                .setVisibility(NotificationCompat.VISIBILITY_SECRET);

        //// フォアグラウンドでサービスを開始＆通知
        Notification notification = notifBuilder.build();

        int notificationId = this.getResources().getInteger(R.integer.id_notification_runningService);
        this.startForeground(notificationId, notification);

        // ----------------------------------
        //
        // ----------------------------------
        //// 通常の場合
        if ( mSp.getBoolean(PREFERENCE_KEY_WHEN_SCREEN_ON, false) ) {
            this.setScreenOnListener();
        }
        if ( mSp.getBoolean(PREFERENCE_KEY_WHEN_TIMER_CALLS, false) ) {
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
        return this.mBinder;
    }

    /************************************
     * 設定画面の値が変更されたときに呼ばれるコールバック（自作）
     * @param preferenceKey SharedPreferenceのキー名
     */
    public void onSPChanged(String preferenceKey) {
        // ----------------------------------
        // 例外処理
        // ----------------------------------
        if ( !mIsStarted ) {
            //開始されたサービス（通常サービス）が起動中でないときは途中で切り上げ
            return;
        }

        // ----------------------------------
        // 通常処理
        // ----------------------------------
        if (preferenceKey.equals(PREFERENCE_KEY_WHEN_SCREEN_ON)) {
            // 電源ON設定がONのとき設定
            if ( mSp.getBoolean(PREFERENCE_KEY_WHEN_SCREEN_ON, false) ) {
                setScreenOnListener();
            } else {
                unsetScreenOnListener();
            }

        } else if ( preferenceKey.equals(PREFERENCE_KEY_WHEN_TIMER_CALLS) ) {
            // 時間設定
            if ( mSp.getBoolean(PREFERENCE_KEY_WHEN_TIMER_CALLS, false) ) {
                setTimerListener();
            } else {
                unsetTimerListener();
            }

        } else if ( preferenceKey.equals(PREFERENCE_KEY_START_TIME)
                || preferenceKey.equals(PREFERENCE_KEY_TIMER_INTERVAL) ) {
            if ( mSp.getBoolean(PREFERENCE_KEY_WHEN_TIMER_CALLS, false) ) {
                unsetTimerListener();
                setTimerListener();
            }
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
        this.registerReceiver(this.mOnOffWPChangeReceiver, intentFilter);
    }
    /************************************
     * 画面ON時壁紙変更のイベントリスナー削除
     */
    private void unsetScreenOnListener() {
        this.unregisterReceiver(this.mOnOffWPChangeReceiver);
    }


    /**
     * 時間で壁紙チェンジのリスナーをセット
     */
    public void setTimerListener() {
        // ----------------------------------
        // 設定値取得
        // ----------------------------------
        final long intervalMillis = Long.parseLong(mSp.getString(
                PREFERENCE_KEY_TIMER_INTERVAL,
                getString(R.string.setting_when_timer_interval_values_default)) );
        final long startTimeUnixTime = mSp.getLong(
                PREFERENCE_KEY_START_TIME, System.currentTimeMillis() );

        final long wpChangeUnixTimeMsec = calcNextWpChangeUnixTimeMsec(
                startTimeUnixTime, intervalMillis, System.currentTimeMillis());

        // ----------------------------------
        //
        // ----------------------------------
        // am はシングルトンなので都度取得でOK
        AlarmManager am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        if (am != null) {
            am.setRepeating (AlarmManager.RTC_WAKEUP, wpChangeUnixTimeMsec,
                    intervalMillis, mWpChangePdIntent);
        }
    }

    public static long calcNextWpChangeUnixTimeMsec(
            long startUnixTimeMsec, long periodMsec, long nowUnixTimeMsec) {
        // xxxは整数
        // startUnixTimeMsec + (xxx-1) * periodMsec < nowUnixTimeMsec < startUnixTimeMsec + xxx * periodMsec
        // (xxx-1) * periodMsec < nowUnixTimeMsec - startUnixTimeMsec < xxx * periodMsec
        // xxx-1 < (nowUnixTimeMsec - startUnixTimeMsec)/periodMsec < xxx

        long xxx = (long)Math.ceil(
                (double)(nowUnixTimeMsec - startUnixTimeMsec)/periodMsec );

        return startUnixTimeMsec + xxx * periodMsec;

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
     * 設定タイマー時壁紙変更のイベントリスナー削除
     */
    public void unsetTimerListener() {
        AlarmManager am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        if (am != null) {
            am.cancel(mWpChangePdIntent);
        }
    }

}
