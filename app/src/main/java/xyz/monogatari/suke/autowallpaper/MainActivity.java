package xyz.monogatari.suke.autowallpaper;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import xyz.monogatari.suke.autowallpaper.service.MainService;

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
    @SuppressWarnings("WeakerAccess")
    public void onOffService_onClick(@SuppressWarnings("unused") View view){

        // -------------------------------------------------
        // サービスが停止中のとき OFFにする
        // -------------------------------------------------
        if ( this.isServiceRunning(MainService.class) ) {
            this.stopService(serviceIntent);
            this.serviceOnOffButton.setText(R.string.off_to_on);

        // -------------------------------------------------
        // サービスが停止中のとき ONにする
        // -------------------------------------------------
        } else {
            // ----------------------------------
            // ストレージのパーミッションが許可されていないときの例外処理
            // (参考)https://developer.android.com/training/permissions/requesting.html?hl=ja
            // ----------------------------------
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            // ディレクトリから壁紙取得がONのとき、かつディレクトリアクセスパーミッションがOFFのとき
            if ( sp.getBoolean(SettingsFragment.KEY_FROM_DIR, false)
                    && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED
                            ) {
                /////shouldのとき
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, this.getString(R.string.permission_toast), Toast.LENGTH_LONG).show();
                }

                // パーミッション許可ダイアログを表示
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        RQ);
                return;
            }

            // ----------------------------------
            // 通常処理
            // ----------------------------------
            this.startService(serviceIntent);
            this.serviceOnOffButton.setText(R.string.on_to_off);
        }
    }
    private static final int RQ = 1;

    /**
     * パーミッション許可のダイアログが終わった瞬間（OKもNGもある）
     * @param requestCode パーミッション許可リクエスト時に送ったリクエストコード
     * @param grantResults パーミッション許可リクエスト時に要求したパーミッション
     * @param permissions 許可の結果、PackageManager.PERMISSION_GRANTED or PERMISSION_DENIED
     */
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
Log.d("○" + this.getClass().getSimpleName(), "onRequestPermissionsResult()");
        switch (requestCode) {
            case RQ:
                // パーミッションを許可したとき
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // ボタンを再度クリックする
                    this.onOffService_onClick(
                            this.findViewById(R.id.main_onOff_service) );
                }
                break;
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
