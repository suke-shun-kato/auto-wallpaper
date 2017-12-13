package xyz.monogatari.suke.autowallpaper;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * 設定画面のアクティビティ
 * Created by k-shunsuke on 2017/12/08.
 */
public class SettingsActivity extends AppCompatActivity {
    // --------------------------------------------------------------------
    // フィールド
    // --------------------------------------------------------------------
    /** バインド先のサービスのインスタンス */
    private MainService mainService;
    /** バインドされた状態か */
    private boolean isBound = false;

    /** ServiceConnectionを継承したクラスのインスタンス */
    private final ServiceConnection myConnection = new ServiceConnection() {

        /**
         * サービスへバインドされたときのコールバック
         * サービス側から実行されるコールバック
         * @param serviceClassName サービスのクラス名
         * @param service サービスから送られてくるバインダー
         */
        @Override
        public void onServiceConnected(ComponentName serviceClassName, IBinder service) {
Log.d("○" + this.getClass().getSimpleName(), "onServiceConnected() 呼ばれた: サービスとバインド成立だよ、サービス名→ "+serviceClassName);

            MainService.MainServiceBinder serviceBinder = (MainService.MainServiceBinder) service;
            SettingsActivity.this.mainService = serviceBinder.getService();
            SettingsActivity.this.isBound = true;
        }

        /**
         * サービスのプロセスがクラッシュしたりKILLされたりしたときに呼ばれるコールバック
         * ※通常にアンバインドされたときは呼ばれない
         * @param serviceClassName サービスのクラス名
         */
        @Override
        public void onServiceDisconnected(ComponentName serviceClassName) {
Log.d("○" + this.getClass().getSimpleName(), "onServiceDisconnected() 呼ばれた: サービスがクラッシュしたよ");
            SettingsActivity.this.isBound = false;
        }
    };

    // --------------------------------------------------------------------
    // メソッド
    // --------------------------------------------------------------------
    /************************************
     * アクティビティが作成されたとき
     * @param savedInstanceState
     */
    @SuppressWarnings("JavaDoc")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
Log.d("○" + this.getClass().getSimpleName(), "onCreate() 呼ばれた: ");
        super.onCreate(savedInstanceState);
Log.d("○" + this.getClass().getSimpleName(), "onCreate() 呼ばれた: super2");
        if ( savedInstanceState == null) {
            this.getFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new SettingsFragment())
                    .commit();
            Log.d("○" + this.getClass().getSimpleName(), "onCreate() 呼ばれた: super3");
        }

    }

    /************************************
     * アクティビティの画面が表示状態になるとき
     */
    @Override
    protected void onStart() {
Log.d("○" + this.getClass().getSimpleName(), "onStart() 呼ばれた");
        super.onStart();

        // ----------------------------------
        // サービスへバインドする
        // ----------------------------------
        Intent intent = new Intent(this, MainService.class);
        this.bindService(intent, this.myConnection, Context.BIND_AUTO_CREATE);
    }

    /************************************
     * 画面が非表示になるとき
     */
    @Override
    protected void onStop() {
Log.d("○" + this.getClass().getSimpleName(), "onStop() 呼ばれた");
Log.d("○" + this.getClass().getSimpleName(), this.isBound+"");
        super.onStop();

        // ----------------------------------
        // サービスへのバインドをやめる
        // ----------------------------------
        if (this.isBound) {
            this.unbindService(this.myConnection);
            this.isBound = false;
        }
Log.d("○" + this.getClass().getSimpleName(), this.isBound+"");
    }

    /************************************
     *
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
Log.d("○" + this.getClass().getSimpleName(), "onSaveInstanceState() 呼ばれた");
        super.onSaveInstanceState(outState);
    }
}
