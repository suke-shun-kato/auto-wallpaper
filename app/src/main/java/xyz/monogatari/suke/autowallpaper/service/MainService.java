package xyz.monogatari.suke.autowallpaper.service;

import android.app.AlarmManager;
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

import xyz.monogatari.suke.autowallpaper.SettingsFragment;

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

    /** SharedPreference */
    private SharedPreferences sp;

    /** 時間設定用のタイマー */
    private Timer timer;

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
        if (this.sp.getBoolean(SettingsFragment.KEY_WHEN_SET_TIME, false)) {
            this.unsetTimerListener();
        }

        this.isStarted = false;

    }
    // --------------------------------------------------------------------
    // メソッド（開始されたサービス（通常サービス）のとき）
    // --------------------------------------------------------------------
    /************************************
     * 通常のサービスを開始したとき
     * @param intent startService() でサービスで送ったIntent
     * @param flags 追加データ、0 か START_FLAG_REDELIVERY か START_FLAG_RETRY
     * @param startId ユニークなID, startService()を重複して実行するたびに ++startId される
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
Log.d("○"+this.getClass().getSimpleName(), "onStartCommand()が呼ばれた hashCode: " + this.hashCode());
Log.d("○"+this.getClass().getSimpleName(), "  flags: "+flags + ", startId: "+ startId);
        this.isStarted = true;

        // ----------------------------------
        //
        // ----------------------------------
        if ( this.sp.getBoolean(SettingsFragment.KEY_WHEN_SCREEN_ON, false) ) {
            this.setScreenOnListener();
        }
        if ( this.sp.getBoolean(SettingsFragment.KEY_WHEN_SET_TIME, false) ) {
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
        // 全てOFFになったとき


        // ----------------------------------
        // from
        // ----------------------------------

        // ----------------------------------
        // When
        // ----------------------------------
        // 電源ON設定がONのとき設定
        if ( this.sp.getBoolean(SettingsFragment.KEY_WHEN_SCREEN_ON, false) ) {
            this.setScreenOnListener();
        } else {
            this.unsetScreenOnListener();
        }
        // 時間設定
        if ( this.sp.getBoolean(SettingsFragment.KEY_WHEN_SET_TIME, false) ) {
            this.setTimerListener();
        } else {
            this.unsetTimerListener();
        }
    }

    // --------------------------------------------------------------------
    // 自作リスナー登録
    // --------------------------------------------------------------------
    /************************************
     * 画面ON時のイベントリスナーを登録
     */
    private void setScreenOnListener() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        this.registerReceiver(this.onOffReceiver, intentFilter);
    }
    /************************************
     * 画面ON時のイベントリスナーの登録を外す
     */
    private void unsetScreenOnListener() {
        this.unregisterReceiver(this.onOffReceiver);
    }

    private void setTimerListener() {
        // ----------------------------------
        // タイマーセット
        // ----------------------------------
        timer = new Timer();
        // schedule は実行が遅延したらその後も遅延する、
        // （例）1分間隔のタイマーが10秒遅れで実行されると次のタイマーは1分後に実行される
        // scheduleAtFixedRate は実行が遅延したら遅延を取り戻そうと実行する、
        // （例）1分間隔のタイマーが10秒遅れで実行されると次のタイマーは50秒後に実行される
        timer.scheduleAtFixedRate(
                new TimerTask() {
                    @Override
                    public void run() {
                        Log.d("○△" + getClass().getSimpleName(), "TimerTask.run()");
                    }
                },
                2000,      //2病後
                1000 //1秒間隔
        );

        // ----------------------------------
        // ブロードキャストレシーバーを設置
        // ----------------------------------


//        AlarmManager alarmManager = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
//
//        if (Build.VERSION.SDK_INT <= 18) {   // ～Android 4.3
//            alarmManager.set(, , AlarmManager.RTC, );
//        } else if (19 <= Build.VERSION.SDK_INT && Build.VERSION.SDK_INT <= 22) {// Android4.4～Android 5.1
//            alarmManager.setExact();
//        } else if (23 <= Build.VERSION.SDK_INT ) {  // Android 6.0～
//            alarmManager.setExactAndAllowWhileIdle();
//        }
    }
    private void unsetTimerListener() {
        this.timer.cancel();

    }

    // --------------------------------------------------------------------
    //
    // --------------------------------------------------------------------


}
