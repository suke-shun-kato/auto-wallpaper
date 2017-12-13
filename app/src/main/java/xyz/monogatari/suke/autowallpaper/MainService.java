package xyz.monogatari.suke.autowallpaper;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by k-shunsuke on 2017/12/12.
 * 裏で壁紙を変更するサービス
 */
public class MainService extends Service {
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
        MainService getService() {
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
Log.d("○"+this.getClass().getSimpleName(), "onCreate()が呼ばれた hashCode: " + this.hashCode());
    }

    /************************************
     * サービスが停止時に呼ばれるメソッド
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
Log.d("○"+this.getClass().getSimpleName(), "onDestroy()が呼ばれた hashCode: " + this.hashCode());
    }
    // --------------------------------------------------------------------
    // メソッド（通常サービススタートのとき）
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
        return START_STICKY;
    }


    // --------------------------------------------------------------------
    // メソッド（バインドサービススタートのとき）
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
}
