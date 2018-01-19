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
import android.widget.ImageButton;
import android.widget.Toast;


import xyz.monogatari.suke.autowallpaper.service.MainService;
import xyz.monogatari.suke.autowallpaper.util.ImgGetPorcSet;

public class MainActivity extends AppCompatActivity {
    // --------------------------------------------------------------------
    // フィールド
    // --------------------------------------------------------------------
    /** サービスに渡すIntent、再利用 */
    private Intent serviceIntent;
    /** サービスON,OFFボタンのView、再利用 */
    private ImageButton serviceOnOffButton;
    /** サービスが起動中か */
    private boolean isServiceRunning;

    /** アクティビティ内で使いまわすSharedPreferences、ここでgetDefaultSharedPreferences()はダメ */
    private SharedPreferences sp;

    // --------------------------------------------------------------------
    // 定数
    // --------------------------------------------------------------------
    /** パーミッションリクエスト用のリクエストコード */
    private static final int RQ_CODE_SERVICE = 1;
    private static final int RQ_CODE_ACTIVITY = 2;

    private static final int BTN_OFF = 0;
    private static final int BTN_ON = 1;

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

        // ----------------------------------
        // 変数の初期化
        // ----------------------------------
        //
        this.sp = PreferenceManager.getDefaultSharedPreferences(this);

        // サービス開始用のインテントを作成
        this.serviceIntent = new Intent(this, MainService.class);

        //
        this.isServiceRunning = this.isServiceRunningSystem(MainService.class);

        // ----------------------------------
        // 表示の切り替え
        // ----------------------------------
        this.serviceOnOffButton = findViewById(R.id.btn_main_onOff_service);
        if (this.isServiceRunning) {
            this.serviceOnOffButton.setImageLevel(BTN_ON);
            this.getWindow().setBackgroundDrawableResource(R.color.translucentLight);
        } else {
            this.serviceOnOffButton.setImageLevel(BTN_OFF);
            this.getWindow().setBackgroundDrawableResource(R.color.translucentDark);
        }

        // ----------------------------------
        // 初回起動時のPreferenceのデフォルト値の適用
        // ----------------------------------
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

java.util.logging.Logger.getLogger("org.apache.http.wire").setLevel(java.util.logging.Level.FINEST);
java.util.logging.Logger.getLogger("org.apache.http.headers").setLevel(java.util.logging.Level.FINEST);

System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire", "debug");
System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http", "debug");
System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.headers", "debug");



    }

    /************************************
     * アクティビティが描画される直前
     * ストレージのパーミッションのダイアログを表示する
     */
    @Override
    protected void onStart() {
        super.onStart();
        if ( this.isServiceRunning //サービスが起動中
                && this.sp.getBoolean(SettingsFragment.KEY_FROM_DIR, false) //ディレクトリからがON
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED    //パーミッション許可がNG
                ) {

            //shouldのとき
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(this, this.getString(R.string.permission_toast), Toast.LENGTH_LONG).show();
            }

            // パーミッション許可ダイアログを表示
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    RQ_CODE_ACTIVITY);
        }
    }

    // --------------------------------------------------------------------
    // メソッド、Util
    // --------------------------------------------------------------------
    /************************************
     * とあるサービスが実行中か確認するメソッド
     * （注意）この関数は下記の理由で極力使わないでください、this.isServiceRunning でサービス実行中か確認してください
     * ※バインドされた画面（設定画面）から戻ってきた時にonStart(),onResume()で実行したときは常にtrueになるので注意
     * @param serviceClass 確認したサービスのClassオブジェクト
     */
    private boolean isServiceRunningSystem(Class<?> serviceClass) {
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
    public void onOffService_onClick(@SuppressWarnings("unused") View view) {

        // -------------------------------------------------
        // サービスが停止中のとき OFFにする
        // -------------------------------------------------
        if ( this.isServiceRunning) {
            this.stopService(this.serviceIntent);
            this.serviceOnOffButton.setImageLevel(BTN_OFF);
            this.getWindow().setBackgroundDrawableResource(R.color.translucentDark);
            this.isServiceRunning = false;
        // -------------------------------------------------
        // サービスが停止中のとき ONにする
        // -------------------------------------------------
        } else {
            // ----------------------------------
            // ストレージのパーミッションが許可されていないときの例外処理
            // (参考)https://developer.android.com/training/permissions/requesting.html?hl=ja
            // ----------------------------------
            // ディレクトリから壁紙取得がONのとき、かつディレクトリアクセスパーミッションがOFFのとき
            if ( this.sp.getBoolean(SettingsFragment.KEY_FROM_DIR, false)
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
                        RQ_CODE_SERVICE);
                return;
            }

            // ----------------------------------
            // 通常処理
            // ----------------------------------
            this.startService(this.serviceIntent);
            this.serviceOnOffButton.setImageLevel(BTN_ON);
            this.getWindow().setBackgroundDrawableResource(R.color.translucentLight);
            this.isServiceRunning = true;
        }
    }

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
            case RQ_CODE_SERVICE:
                // パーミッションを許可したとき
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // ボタンを再度クリックする
                    this.onOffService_onClick(
                            this.findViewById(R.id.btn_main_onOff_service) );
                }
                break;
            case RQ_CODE_ACTIVITY:
                // 許可しようがしまいが特になにもしない
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
     * 壁紙セット（変更）ボタンをクリックしたとき
     * @param view 押されたボタンのビュー
     */
    public void setWallpaper_onClick(@SuppressWarnings("unused") View view) {
//        new ImgGetPorcSet(this).execute();
        new ImgGetPorcSet(this).executeNewThread();
    }

    /************************************
     * 履歴画面へのボタンをクリックしたとき
     * @param view 押されたボタンのビュー
     */
    public void toHistory_onClick(@SuppressWarnings("unused") View view) {
        Intent intent = new Intent(this, HistoryActivity.class);
        this.startActivity(intent);

        // ----------------------------------
        //
        // ----------------------------------


    }
}
