package xyz.monogatari.autowallpaper;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import xyz.monogatari.autowallpaper.service.MainService;
import xyz.monogatari.autowallpaper.util.DisplaySizeCheck;
import xyz.monogatari.autowallpaper.util.ProgressBcastReceiver;
import xyz.monogatari.autowallpaper.wpchange.WpManagerService;


public class MainActivity extends AppCompatActivity {
    // --------------------------------------------------------------------
    // フィールド
    // --------------------------------------------------------------------
    /** サービスに渡すIntent、再利用 */
    private Intent serviceIntent;
    /** サービスON,OFFボタンのView、再利用 */
    private ImageButton serviceOnOffButton;

    private TextView nextWpSetTextView;
    /** サービスが起動中か, null代入必須、コンパイルエラーが出る */
    private boolean isServiceRunning = false;

    /** アクティビティ内で使いまわすSharedPreferences、ここでgetDefaultSharedPreferences()はダメ */
    private SharedPreferences sp;

    private ProgressBcastReceiver progressBcastReceiver;

    /** メインサービスのオブジェクト */
    @SuppressWarnings("FieldCanBeLocal")
    private MainService mainService;
    /** メインサービスがこのアクティビティにバインドされているか */
    private boolean isBound;

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
            Log.d("○" + MainActivity.this.getClass().getSimpleName(), "onServiceConnected() 呼ばれた: サービスとバインド成立だよ、サービス名→ "+serviceClassName);

            // ----------
            // フィールドをセット
            // ----------
            MainService.MainServiceBinder serviceBinder = (MainService.MainServiceBinder) service;
            mainService = serviceBinder.getService();
            isServiceRunning = true;

            // ----------
            // メインサービスONのときの表示の設定など
            // ----------
            // ボタン
            serviceOnOffButton.setImageLevel(BTN_ON);
            // 背景
            getWindow().setBackgroundDrawableResource(R.color.translucentLight);
            // 次の壁紙変更時間
            nextWpSetTextView.setText(getNextWpChangeText());

            // ----------
            // パーミッション許可ダイアログを表示
            // ----------
//            if ( isServiceRunning //サービスが起動中
//                    && sp.getBoolean(SettingsFragment.KEY_FROM_DIR, false) //ディレクトリからがON
//                    && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
//                    != PackageManager.PERMISSION_GRANTED    //パーミッション許可がNG
//                    ) {
//
//                //shouldのとき
//                if (ActivityCompat.shouldShowRequestPermissionRationale(
//                        MainActivity.this,
//                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
//                    Toast.makeText(MainActivity.this, getString(R.string.permission_toast), Toast.LENGTH_LONG).show();
//                }
//
//                // パーミッション許可ダイアログを表示
//                ActivityCompat.requestPermissions(
//                        MainActivity.this,
//                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
//                        RQ_CODE22_ACTIVITY);
//            }
        }

        /**
         * サービスのプロセスがクラッシュしたりKILLされたりしたときに呼ばれるコールバック
         * ※通常にアンバインドされたときは呼ばれない
         * @param serviceClassName サービスのクラス名
         */
        @Override
        public void onServiceDisconnected(ComponentName serviceClassName) {
            Log.d("○________________" + this.getClass().getSimpleName(), "onServiceDisconnected() 呼ばれた: サービスがクラッシュしたよ");
            isBound = false;
            isServiceRunning = false;
        }
    };

    // --------------------------------------------------------------------
    // 定数
    // --------------------------------------------------------------------
    /** パーミッションリクエスト用のリクエストコード */
    private static final int REQUEST_PERMISSION_ONOFF_SERVICE = 1;
    private static final int REQUEST_PERMISSION_SET_WALLPAPER = 2;

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
        // ----------------------------------
        //
        // ----------------------------------
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
Log.d("○" + this.getClass().getSimpleName(), "onCreate() 呼ばれた: " + R.layout.activity_main);

        // ----------------------------------
        // アクションバーの設定
        // ----------------------------------
        ////　ツールバーをアクションバーとして表示
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        this.setSupportActionBar(myToolbar);

        // ----------------------------------
        // 変数の初期化
        // ----------------------------------
        //
        this.sp = PreferenceManager.getDefaultSharedPreferences(this);

        // サービス開始用のインテントを作成
        this.serviceIntent = new Intent(this, MainService.class);

        //
//        this.isServiceRunning = this.isServiceRunningSystem(MainService.class);

        // ----------------------------------
        // Viewなどの表示の動的な設定
        // ----------------------------------
        //// テキスト表示
        this.nextWpSetTextView = this.findViewById(R.id.main_text_next_set);

        //// ON/OFFボタン
        this.serviceOnOffButton = this.findViewById(R.id.btn_main_onOff_service);

        // ----------------------------------
        // 初回起動時のPreferenceのデフォルト値の適用
        // ----------------------------------
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);


        // ----------------------------------
        // 壁紙変更中のプログレスバー用のBcastレシーバーを登録
        // これは必ずonCreateで行うこと、onStartで登録→onStopで解除などすると別画面でサービスOFFのブロードキャストが検知できなくなるため
        // ----------------------------------
        this.progressBcastReceiver = new ProgressBcastReceiver();
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(WpManagerService.ACTION_NAME);
        this.registerReceiver(this.progressBcastReceiver, iFilter);

        // ----------------------------------
        //
        // ----------------------------------
Log.d("○" + this.getClass().getSimpleName(), "ディスプレイの横幅: " + DisplaySizeCheck.getScreenWidthInDPs(this) + "dp");

//java.util.logging.Logger.getLogger("org.apache.http.wire").setLevel(java.util.logging.Level.FINEST);
//java.util.logging.Logger.getLogger("org.apache.http.headers").setLevel(java.util.logging.Level.FINEST);

System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire", "debug");
System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http", "debug");
System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.headers", "debug");
    }


//
//    static void loggggg(long unixTimeMsec) {
//        Date date = new Date(unixTimeMsec);
//        Date date2 = new Date(unixTimeMsec + TimeZone.getDefault().getRawOffset() + TimeZone.getDefault().getDSTSavings());
////        Date date2 = new Date(unixTimeMsec + TimeZone.getDefault().getRawOffset());
//
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
//        sdf.setTimeZone(TimeZone.getTimeZone("GMT+9"));
//
//        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
//        sdf2.setTimeZone(TimeZone.getTimeZone("GMT+0"));
//
//        SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
//        sdf3.setTimeZone(TimeZone.getTimeZone("GMT-8"));
//
//
//
//
//        Log.d("○MainActivity", "__"+sdf.format(date));
//        Log.d("○MainActivity", "________________________"+sdf2.format(date2));
//        Log.d("○MainActivity", "________________________"+sdf2.format(date));
//        Log.d("○MainActivity", "________________________"+sdf3.format(date));
//    }

    /************************************
     * アクティビティが描画される直前
     * ストレージのパーミッションのダイアログを表示する
     */
    @Override
    protected void onStart() {
Log.d("○"+this.getClass().getSimpleName(), "onStart()");
        super.onStart();
        // ----------------------------------
        //
        // ----------------------------------
        Intent intent = new Intent(this, MainService.class);

        // flags:0 だと自動でstartService()が開始されない（戻り値はサービス開始されていなくてもバインド成功したらtrueが返る）
        // Context.BIND_AUTO_CREATEだと自動開始される
        this.isBound = this.bindService(intent, this.myConnection, 0);
//        boolean rtnBool = this.bindService(intent, this.myConnection, Context.BIND_AUTO_CREATE);

        // ----------------------------------
        // 表示関連の更新
        // ----------------------------------
//        if (this.isServiceRunning) {
//            this.nextWpSetTextView.setText(this.getNextWpChangeText());
//        }

    }

    @Override
    protected void onStop() {
        super.onStop();
Log.d("○"+this.getClass().getSimpleName(), "onStop()");
        if (this.isBound) {
            this.unbindService(this.myConnection);
            this.isBound = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
Log.d("○"+this.getClass().getSimpleName(), "onDestroy()");
        this.unregisterReceiver(this.progressBcastReceiver);
    }

    // --------------------------------------------------------------------
    // メソッド、Util
    // --------------------------------------------------------------------
//    /************************************
//     * とあるサービスが実行中か確認するメソッド
//     * （注意）この関数は下記の理由で極力使わないでください、this.isServiceRunning でサービス実行中か確認してください
//     * ※バインドされた画面（設定画面）から戻ってきた時にonStart(),onResume()で実行したときは常にtrueになるので注意
//     * @param serviceClass 確認したサービスのClassオブジェクト
//     */
//    private boolean isServiceRunningSystem(Class<?> serviceClass) {
//        ActivityManager manager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
//        if (manager == null ){
//            return false;
//        }
//
////for (ActivityManager.RunningServiceInfo serviceInfo : manager.getRunningServices(Integer.MAX_VALUE) ) {
////    Log.d("○□□", serviceInfo.service.getClassName());
////}
//        for (ActivityManager.RunningServiceInfo serviceInfo : manager.getRunningServices(Integer.MAX_VALUE) ) {
//            if (serviceClass.getName().equals(serviceInfo.service.getClassName())) {
//                return true;
//            }
//        }
//        return false;
//    }

    // --------------------------------------------------------------------
    // メソッド、ボタン押したときのイベントハンドラ
    // --------------------------------------------------------------------
    /************************************
     *
     */
    @SuppressWarnings("WeakerAccess")
    public void onOffService_onClick(@SuppressWarnings("unused") View view) {
        // -------------------------------------------------
        // サービスが実行中のとき OFFにする
        // -------------------------------------------------
        if ( this.isServiceRunning) {
            this.stopService(this.serviceIntent);

            //// 文字列
            this.nextWpSetTextView.setText("");

            //// ボタンと背景の設定
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

                PermissionManager.showRequestDialog(this, REQUEST_PERMISSION_ONOFF_SERVICE);

                return;

            }

            // ----------------------------------
            // 通常処理
            // ----------------------------------
            this.startService(this.serviceIntent);

            //// 文字列
            this.nextWpSetTextView.setText(this.getNextWpChangeText());

            //// ボタンと背景
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
    @SuppressWarnings("unused")
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
Log.d("○" + this.getClass().getSimpleName(), "onRequestPermissionsResult()");
        switch (requestCode) {
            case REQUEST_PERMISSION_ONOFF_SERVICE:   //サービスのON/OFFボタンを押してパーミッション許可したとき
                // パーミッションを許可したとき
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // ボタンを再度クリックする
                    this.onOffService_onClick(
                            this.findViewById(R.id.btn_main_onOff_service) );
                }
                break;

            case REQUEST_PERMISSION_SET_WALLPAPER:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // ボタンを再度クリックする
                    this.setWallpaper(this);
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
     * 壁紙セット（変更）ボタンをクリックしたとき（初期設定、壁紙変更中はonProgressVisibleでリスナー解除される）
     * @param view 押されたボタンのビュー
     */
    public void setWallpaper_onClick(@SuppressWarnings("unused") View view) {
        if ( this.sp.getBoolean(SettingsFragment.KEY_FROM_DIR, false)
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                ) {

            PermissionManager.showRequestDialog(this, REQUEST_PERMISSION_SET_WALLPAPER);

            return;
        }

        this.setWallpaper(this);
    }

    /************************************
     * 壁紙を変更するサービスを実行する、ボタンが押されたときにリスナー
     */
    private void setWallpaper(Context packageContext) {
        Intent i = new Intent(packageContext, WpManagerService.class);
        startService(i);
    }

    // --------------------------------------------------------------------
    //
    // --------------------------------------------------------------------
    /************************************
     * 次の壁紙交代のタイミングのテキストを作成する関数
     * @return 作成した文字列
     */
    private String getNextWpChangeText() {
        List<String> list = new ArrayList<>();

        // ----------------------------------
        // 電源OFF時に変更
        // ----------------------------------
        if ( this.sp.getBoolean(SettingsFragment.KEY_WHEN_SCREEN_ON, false) ) {
            list.add(this.getString(R.string.main_next_wallpaper_set_screenOff));
        }

        // ----------------------------------
        // 設定時間で変更
        // ----------------------------------
        if ( this.sp.getBoolean(SettingsFragment.KEY_WHEN_TIMER, false) ) {
            //// 遅延時間を計算
            long intervalMsec = Long.parseLong(this.sp.getString(
                    SettingsFragment.KEY_WHEN_TIMER_INTERVAL, ""
            ));
            long settingUnixTimeMsec = this.sp.getLong(SettingsFragment.KEY_WHEN_TIMER_START_TIMING_0, System.currentTimeMillis());
            long delayMsec = MainService.calcDelayMsec(settingUnixTimeMsec, intervalMsec, System.currentTimeMillis());

            //// 表示を取得
            long nextUnixTimeMsec = delayMsec + System.currentTimeMillis();
            String nextDateText = DateUtils.formatDateTime(
                    this,
                    nextUnixTimeMsec,
                    DateUtils.FORMAT_SHOW_DATE
                            | DateUtils.FORMAT_SHOW_WEEKDAY
                            | DateUtils.FORMAT_SHOW_TIME
                            | DateUtils.FORMAT_NUMERIC_DATE   //曜日表示の省略
                            | DateUtils.FORMAT_ABBREV_ALL   //曜日表示の省略
            );
            list.add(nextDateText);
        }

        // ----------------------------------
        // 文字列結合してreturn
        // ----------------------------------[
        if (list.size() == 0) {
            return this.getString(R.string.main_next_wallpaper_set_no);
        } else {
            StringBuilder sb = new StringBuilder();
            String glue = this.getString(R.string.main_next_wallpaper_set_glue);
            for(String str : list) {
                sb.append(glue).append(str);
            }
            return sb.substring(glue.length());
        }
    }
    // --------------------------------------------------------------------
    // ブロードキャストレシーバー受信時の挙動設定
    // --------------------------------------------------------------------
    public void onWpChanging() {
        //// プログレスバー（グルグル）を表示する
        View progressView = this.findViewById(R.id.main_setWallpaper_progress);
        progressView.setVisibility(ProgressBar.VISIBLE);

        //// 壁紙セットボタンを押せないようにする
        View btnView = this.findViewById(R.id.btn_main_change_wallpaper);
        btnView.setOnClickListener(null);
    }


    public void onWpChangeDone() {
        //// プログレスバー（グルグル）を非表示にする
        View v = this.findViewById(R.id.main_setWallpaper_progress);
        v.setVisibility(ProgressBar.GONE);

        //// 壁紙セットボタンを押せるようにする
        View btnView = this.findViewById(R.id.btn_main_change_wallpaper);
        btnView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                setWallpaper(MainActivity.this);
            }
        });

        //// 次の時間表示を更新する
        if (this.isServiceRunning) {
            this.nextWpSetTextView.setText(this.getNextWpChangeText());
        }
    }

    public void onWpChangeError () {
Log.d("○" + this.getClass().getSimpleName(), "onWpChangeError()ですよ！！！");
        Toast.makeText(this, R.string.main_toast_no_image, Toast.LENGTH_SHORT).show();
    }



    /************************************
     * 履歴画面へのボタンをクリックしたとき
     * @param view 押されたボタンのビュー
     */
    public void toHistory_onClick(@SuppressWarnings("unused") View view) {
        Intent intent = new Intent(this, HistoryActivity.class);
        this.startActivity(intent);
    }
}
