package xyz.monogatari.suke.autowallpaper;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    // --------------------------------------------------------------------
    // フィールド
    // --------------------------------------------------------------------
    /** サービスに渡すIntent */
    private Intent serviceIntent;
    /** サービスON,OFFボタンのView */
    private Button serviceOnOffButton;

    // --------------------------------------------------------------------
    // メソッド（ライフサイクル）
    // --------------------------------------------------------------------
    /************************************
     * アクティビティ作成時
     * @param savedInstanceState 画像回転時などにアクティビティ破棄時に保存した値
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
Log.d("○" + this.getClass().getSimpleName(), "onCreate() 呼ばれた: " + R.layout.activity_main);

        // サービス開始用のインテントを作成
        this.serviceIntent = new Intent(this, MainService.class);

        this.serviceOnOffButton = findViewById(R.id.main_onOff_service);
        if (this.isServiceRunning(MainService.class)) {
            this.serviceOnOffButton.setText(R.string.on_to_off);
        } else {
            this.serviceOnOffButton.setText(R.string.off_to_on);
        }
    }

    // --------------------------------------------------------------------
    // メソッド、Util
    // --------------------------------------------------------------------
    /************************************
     * とあるサービスが実行中か確認するメソッド
     * @param serviceClass 確認したサービスのClassオブジェクト
     */
    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager == null ){
            return false;
        }

        for (ActivityManager.RunningServiceInfo serviceInfo : manager.getRunningServices(Integer.MAX_VALUE) ) {
            if (serviceClass.getName().equals(serviceInfo.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    // --------------------------------------------------------------------
    // メソッド、ボタン押したときのイベントハンドラ
    // --------------------------------------------------------------------
    /************************************
     *
     */
    public void onOffService_onClick(@SuppressWarnings("unused") View view){
        // サービスが起動中のとき
        if ( this.isServiceRunning(MainService.class) ) {
            this.stopService(serviceIntent);
            this.serviceOnOffButton.setText(R.string.off_to_on);
        // サービスが停止中のとき
        } else {
            this.startService(serviceIntent);
            this.serviceOnOffButton.setText(R.string.on_to_off);
        }
    }

    /************************************
     * 設定画面へのボタンをクリックしたとき
     * @param view 押されたボタンのビュー
     */
    public void toSetting_onClick(@SuppressWarnings("unused") View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        this.startActivity(intent);
    }

    /************************************
     * 設定を消去ボタンをクリックしたとき
     * @param view 押されたボタンのビュー
     */
    public void dellSp_onClick(@SuppressWarnings("unused") View view) {
        // SharedPreferenceを削除
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.clear().apply();

        //
        Toast.makeText(this, "設定を削除（初期化）しました", Toast.LENGTH_SHORT)
                .show();

    }
}
