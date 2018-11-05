package xyz.goodistory.autowallpaper;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import xyz.goodistory.autowallpaper.service.MainService;

/**
 * 設定画面のフラグメント、
 * サービスへのバインドはフラグメントで行う方が良い（違うアクティビティにアタッチされるかもしれないので）
 * Created by k-shunsuke on 2017/12/08.
 */
public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    // --------------------------------------------------------------------
    // フィールド
    // --------------------------------------------------------------------
    /** バインド先のサービスのインスタンス */
    private MainService mainService;
    /** バインドされた状態か */
    private boolean isBound = false;
    private boolean isServiceRunning = false;

    private SharedPreferences sp;

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
            Log.d("○SettingsFragment" + this.getClass().getSimpleName(), "onServiceConnected() 呼ばれた: サービスとバインド成立だよ、サービス名→ "+serviceClassName);

            MainService.MainServiceBinder serviceBinder = (MainService.MainServiceBinder) service;
            mainService = serviceBinder.getService();
            isServiceRunning = true;

        }

        /**
         * サービスのプロセスがクラッシュしたりKILLされたりしたときに呼ばれるコールバック
         * ※通常にアンバインドされたときは呼ばれない
         * @param serviceClassName サービスのクラス名
         */
        @Override
        public void onServiceDisconnected(ComponentName serviceClassName) {
            Log.d("○" + this.getClass().getSimpleName(), "onServiceDisconnected() 呼ばれた: サービスがクラッシュしたよ");
            isBound = false;
            isServiceRunning = false;
        }
    };
    
    // --------------------------------------------------------------------
    // 定数
    // --------------------------------------------------------------------
    /** ディレクトリ選択<Preference>のkey名 */
    @SuppressWarnings("unused")
    public static final String KEY_FROM_DIR = "from_dir";
    @SuppressWarnings("WeakerAccess")
    public static final String KEY_FROM_DIR_PATH = "from_dir_path";

    public static final String KEY_FROM_TWITTER_FAV = "from_twitter_fav";
    @SuppressWarnings("WeakerAccess")
    public static final String KEY_FROM_TWITTER_OAUTH = "from_twitter_oauth";

    public static final String KEY_WHEN_SCREEN_ON = "when_turnOn";
    public static final String KEY_WHEN_TIMER = "when_timer";
    public static final String KEY_WHEN_TIMER_START_TIMING_1 = "when_timer_startTiming_1";

    public static final String KEY_WHEN_TIMER_INTERVAL = "when_timer_interval";

    public static final String KEY_OTHER_AUTO_ROTATION = "other_autoRotation";
    @SuppressWarnings("WeakerAccess")
    public static final String KEY_OTHER_ABOUT = "other_about";

    private static final int RQ_CODE_FROM_DIR = 1;
    private static final int RQ_CODE_FROM_DIR_PATH = 2;

    // --------------------------------------------------------------------
    // メソッド
    // --------------------------------------------------------------------
    /************************************
     * フラグメントが作成されたとき
     * @param savedInstanceState アクティビティ破棄時に保存した値
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 設定xmlを読み込む
        this.addPreferencesFromResource(R.xml.preferences);
    }

    public void onNewIntent(Intent intent) {
        // ----------------------------------
        // Twitter認証のコールバックのとき TwitterOAuthPreference にIntentでURLの情報を渡す
        // コールバックではonActivityCreated()は呼ばれないのでこの場所
        // ----------------------------------
        // Twitter認証のコールバックのとき
        if ( intent != null
                && intent.getData() != null
                && intent.getData().toString().startsWith(this.getString(R.string.twitter_callback_url))) {
            ((TwitterOAuthPreference)this.findPreference(KEY_FROM_TWITTER_OAUTH)).onNewIntent(intent);
        }
    }

    /************************************
     * フラグメントが表示される直前
     */
    @Override
    public void onStart() {
        super.onStart();
        // ----------------------------------
        // サービスへバインドする
        // ↓公式でonStart()のタイミングでバインドしている（Activityだけど）のでこの場所
        // https://developer.android.com/guide/components/bound-services.html?hl=ja
        // ----------------------------------
        Activity attachedActivity = this.getActivity();
        Intent intent = new Intent(attachedActivity, MainService.class);

//        attachedActivity.bindService(intent, this.myConnection, Context.BIND_AUTO_CREATE);
        this.isBound  = attachedActivity.bindService(intent, this.myConnection, 0);
    }



    /************************************
     * フラグメントが非表示になる直前
     */
    @Override
    public void onStop() {
        super.onStop();

        // ----------------------------------
        // サービスへのバインドをやめる
        // ----------------------------------
        if (this.isBound) {
            this.getActivity().unbindService(this.myConnection);
            this.isBound = false;
        }
    }

    /************************************
     * Preference.setOnPreferenceClickListener
     * フラグメントに関連づいたView層を生成する直前
     */
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // ----------------------------------
        // タイトル表示の設定
        // ----------------------------------
        //// About
        this.findPreference(KEY_OTHER_ABOUT).setTitle(
                String.format( getString(R.string.setting_other_about_title), getString(R.string.app_name) )
        );


        // ----------------------------------
        // サマリーの表示の設定
        // ----------------------------------
        this.sp = PreferenceManager.getDefaultSharedPreferences( this.getActivity() );

        //// 選択ディレクトリ
        Preference fromDirPathPref = this.findPreference(KEY_FROM_DIR_PATH);
        String str = this.sp.getString(KEY_FROM_DIR_PATH, this.getString(R.string.setting_from_dir_which_default_summary) );

        fromDirPathPref.setSummary( str );

        //// Twitter認証
        TwitterOAuthPreference twitterPref = (TwitterOAuthPreference)this.findPreference(KEY_FROM_TWITTER_OAUTH);
        if ( twitterPref.hasAccessToken() ) {
            twitterPref.setSummary(R.string.setting_from_twitter_oauth_summary_done);
        } else {
            twitterPref.setSummary(R.string.setting_from_twitter_oauth_summary_notYet);
        }


        // ----------------------------------
        // <Preference>のイベントリスナの設定、主にパーミッションダイアログ表示用
        // ----------------------------------
        // ----------
        // 「ディレクトリから」 のパーミッションダイアログ表示設定
        // ----------
        // ここは setOnPreferenceClickListener() ではない
        this.findPreference(KEY_FROM_DIR).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener(){
            /************************************
             * @param preference クリックされたPreference
             * @param newValue Preferenceの新しい値
             * @return true:値変更を反映、false:反映しない
             */
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                // ----------
                // パーミッション許可ダイアログを出るようにしている
                // ----------
                //// 設定がONになるとき、かつAndroid6.0のとき、かつストレージアクセスの許可を得ていないとき、
                if ( (boolean)newValue  //設定がOFF→ONになるとき
                  &&  ContextCompat.checkSelfPermission(
                                SettingsFragment.this.getActivity(),
                                Manifest.permission.READ_EXTERNAL_STORAGE
                       )
                        != PackageManager.PERMISSION_GRANTED
                ) {
                    PermissionManager.showRequestDialog(getActivity(), RQ_CODE_FROM_DIR);
//                    // パーミッション必要な理由を表示
//                    toastIfShould(SettingsFragment.this);
//
//                    // アクセス許可を要求（ダイアログを表示）
//                    SettingsFragment.this.requestPermissions(
//                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
//                            RQ_CODE_FROM_DIR
//                    );
                    return false;

                } else {
                    return true;
                }
            }
        });
        // ----------
        // Twitterのお気に入りからをクリックしたとき
        // ----------
        this.findPreference(KEY_FROM_TWITTER_FAV).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener(){
            /************************************
             * @param preference クリックされたPreference
             * @param newValue Preferenceの新しい値
             * @return true:値変更を反映、false:反映しない
             */
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if ( sp.getString(KEY_FROM_TWITTER_OAUTH, null) == null ) {
                    // Twitterの認証が未だのとき
                    Toast.makeText(getActivity(), R.string.setting_form_twitter_fav_error, Toast.LENGTH_SHORT).show();
                    return false;
                } else {
                    return true;
                }
            }
        });

        // ----------
        // 「ディレクトリを設定」 のパーミッションダイアログ表示設定
        // ----------
        this.findPreference(KEY_FROM_DIR_PATH).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener(){
            /************************************
             * Preferenceがクリックされたときのコールバック
             * @param preference クリックされたプリファレンス
             * @return true:正常にクリックされた動作が実行されるとき、false: されないとき
             */
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if ( ContextCompat.checkSelfPermission(SettingsFragment.this.getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                       != PackageManager.PERMISSION_GRANTED
                  ||
                     ContextCompat.checkSelfPermission(SettingsFragment.this.getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED

                ) {
                    PermissionManager.showRequestDialog(getActivity(), RQ_CODE_FROM_DIR_PATH);

                    return false;
                } else {
                    return true;
                }
            }
        });

        // ----------
        //
        // ----------
        this.findPreference(KEY_OTHER_ABOUT).setOnPreferenceClickListener(
                new Preference.OnPreferenceClickListener(){
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Intent i = new Intent(getActivity(), AboutActivity.class);
                        startActivity(i);
                        return true;
                    }
                }
        );



        // ----------------------------------
        //
        // ----------------------------------
        return super.onCreateView(inflater, container, savedInstanceState);
    }
//    /************************************
//     * パーミッションがいる説明をトーストで表示する（表示しないといけない場合）
//     */
//    private void toastIfShould(SettingsFragment myThis) {
//        if (ActivityCompat.shouldShowRequestPermissionRationale(
//                myThis.getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
//                ){
//            Toast.makeText(myThis.getActivity(),
//                    myThis.getString(R.string.permission_toast),
//                    Toast.LENGTH_LONG)
//                    .show();
//        }
//    }

    // --------------------------------------------------------------------
    // メソッド、設定の変更感知用
    // --------------------------------------------------------------------
    /************************************
     * フラグメントが利用可能状態になる直前、
     * アクティビティがフォアグラウンドになるとき
     */
    @Override
    public void onResume() {
        super.onResume();

        //// 設定変更リスナーを設置
        this.getPreferenceScreen()
                .getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    /**********************************
     * フラグメントが一時停止になったとき、アクティビティがバックグラウンドになるとき
     */
    @Override
    public void onPause() {
        super.onPause();
        this.getPreferenceScreen()
                .getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    // --------------------------------------------------------------------
    // メソッド
    // --------------------------------------------------------------------
    /************************************
     * パーミッション許可のダイアログが終わった瞬間（OKもNGもある）
     * @param requestCode パーミッション許可リクエスト時に送ったリクエストコード
     * @param grantResults パーミッション許可リクエスト時に要求したパーミッション
     * @param permissions 許可の結果、PackageManager.PERMISSION_GRANTED or PERMISSION_DENIED
     */
    public void onRequestPermissionsResultFragment(
            int requestCode, @SuppressWarnings("unused") @NonNull String[] permissions, @NonNull int[] grantResults
    ) {
        switch (requestCode) {
            case RQ_CODE_FROM_DIR:
                // 許可をクリックしたとき
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    // ディレクトリから の設定をONにする
                    ((SwitchPreference)this.findPreference(KEY_FROM_DIR)).setChecked(true);
                    // SharedPreferenceが変更したときのイベントを発火
                    this.onSharedPreferenceChanged(this.sp, KEY_FROM_DIR);
                }
                break;
            case RQ_CODE_FROM_DIR_PATH:
                // 許可をクリックしたとき
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    // もう一度Preferenceをクリックする
                    ((SelectDirPreference)this.findPreference(KEY_FROM_DIR_PATH)).click();
                }
                break;
        }
    }

    /**
     *
     * 設定変更したときのイベントハンドラ、OnSharedPreferenceChangeListenerのメソッド
     * @param sp SharedPreferences、保存された設定のオブジェクト
     * @param key 設定の値を取り出すためのkey, このkeyの設定が変更された
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
        // ----------------------------------
        // 設定値をSummaryに反映
        // ----------------------------------
        switch (key) {
            //// ディレクトリ選択
            case KEY_FROM_DIR_PATH:
                Preference fromDirPathPreference = this.findPreference(key);
                fromDirPathPreference.setSummary(sp.getString(key, ""));
                break;
            //// Twitter認証
            case KEY_FROM_TWITTER_OAUTH:    //Twitter認証完了後にサマリーが認証完了になるようにする
                Preference fromTwitterOauthPreference = this.findPreference(key);
                fromTwitterOauthPreference.setSummary(R.string.setting_from_twitter_oauth_summary_done);
                break;

        }

        // ----------------------------------
        // ボタンが切り替わったことをサービスに伝える
        // ----------------------------------
        if (this.isServiceRunning) {
            this.mainService.onSPChanged(key);
        }

    }

}




