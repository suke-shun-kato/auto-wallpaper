package xyz.monogatari.suke.autowallpaper;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import xyz.monogatari.suke.autowallpaper.service.MainService;

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
            Log.d("○" + this.getClass().getSimpleName(), "onServiceConnected() 呼ばれた: サービスとバインド成立だよ、サービス名→ "+serviceClassName);

            MainService.MainServiceBinder serviceBinder = (MainService.MainServiceBinder) service;
            SettingsFragment.this.mainService = serviceBinder.getService();
            SettingsFragment.this.isBound = true;
        }

        /**
         * サービスのプロセスがクラッシュしたりKILLされたりしたときに呼ばれるコールバック
         * ※通常にアンバインドされたときは呼ばれない
         * @param serviceClassName サービスのクラス名
         */
        @Override
        public void onServiceDisconnected(ComponentName serviceClassName) {
            Log.d("○" + this.getClass().getSimpleName(), "onServiceDisconnected() 呼ばれた: サービスがクラッシュしたよ");
            SettingsFragment.this.isBound = false;
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
    @SuppressWarnings("unused")
    public static final String KEY_FROM_TWITTER_FAV = "from_twitter_fav";
    @SuppressWarnings("WeakerAccess")
    public static final String KEY_FROM_TWITTER_OAUTH = "from_twitter_oauth";

    public static final String KEY_WHEN_SCREEN_ON = "when_turnOn";
    public static final String KEY_WHEN_TIMER = "when_timer";
    public static final String KEY_WHEN_TIMER_START_TIME = "when_timer_startTime";
    public static final String KEY_WHEN_TIMER_INTERVAL = "when_timer_interval";

    public static final String KEY_OTHER_AUTO_ROTATION = "other_autoRotation";

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
Log.d("○" + this.getClass().getSimpleName(), "onCreate()が呼ばれた:start");
        super.onCreate(savedInstanceState);

        // 設定xmlを読み込む
        this.addPreferencesFromResource(R.xml.preferences);
Log.d("○" + this.getClass().getSimpleName(), "onCreate()が呼ばれた:end");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
Log.d("○"+this.getClass().getSimpleName(), "onActivityCreated():start");
        super.onActivityCreated(savedInstanceState);
    }

    /************************************
     * フラグメントが表示される直前
     */
    @Override
    public void onStart() {
Log.d("○" + this.getClass().getSimpleName(), "onStart()が呼ばれた（先頭）");
        super.onStart();

        Intent getIntent = this.getActivity().getIntent();
//String a;
//boolean s = (a == null);
//String b = "aaaaa";
//a = "cccc";
//b = "fffff";
        // ----------------------------------
        // Twitter認証のコールバックのとき TwitterOAuthPreference にIntentでURLの情報を渡す
        // コールバックではonActivityCreated()は呼ばれないのでこの場所
        // ----------------------------------
        // Twitter認証のコールバックのとき
        if ( getIntent != null
                && getIntent.getData() != null
                && getIntent.getData().toString().startsWith(TwitterOAuthPreference.CALLBACK_URL)) {
            ((TwitterOAuthPreference)this.findPreference(KEY_FROM_TWITTER_OAUTH)).onNewIntent(getIntent);
        }

        // ----------------------------------
        // サービスへバインドする
        // ↓公式でonStart()のタイミングでバインドしている（Activityだけど）のでこの場所
        // https://developer.android.com/guide/components/bound-services.html?hl=ja
        // ----------------------------------
        Activity attachedActivity = this.getActivity();
        Intent intent = new Intent(attachedActivity, MainService.class);
        attachedActivity.bindService(intent, this.myConnection, Context.BIND_AUTO_CREATE);
    }



    /************************************
     * フラグメントが非表示になる直前
     */
    @Override
    public void onStop() {
Log.d("○" + this.getClass().getSimpleName(), "onStop()が呼ばれた");
        super.onStop();

        // ----------------------------------
        // サービスへのバインドをやめる
        // ----------------------------------
        if (this.isBound) {
            this.getActivity().unbindService(this.myConnection);
            this.isBound = false;
        }
    }

    private void setStartTimePreference() {
        ListPreference startTimeLP = (ListPreference)this.findPreference(KEY_WHEN_TIMER_START_TIME);
        ListPreference intervalLP = (ListPreference)this.findPreference(KEY_WHEN_TIMER_INTERVAL);

        startTimeLP.setEntryValues(new String[]{
                this.getString(R.string.setting_when_timer_startTime_values_0),
                intervalLP.getValue()
        });
        startTimeLP.setEntries(new String[]{
                this.getString(R.string.setting_when_timer_startTime_entries_0),
                (String)intervalLP.getEntry()
        });
    }
    /************************************
     * Preference.setOnPreferenceClickListener
     * フラグメントに関連づいたView層を生成する直前
     */
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
Log.d("○"+this.getClass().getSimpleName(), "onCreateView() 呼ばれた（先頭）");
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
        //
        // ----------------------------------
        this.setStartTimePreference();

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
Log.d("○OnPreferenceChangeL", "onPreferenceChange() 呼ばれた: "+(boolean)newValue);
                // ----------
                // パーミッション許可ダイアログを出るようにしている
                // ----------
                //// 設定がONになるとき、かつAndroid6.0のとき、かつストレージアクセスの許可を得ていないとき、
                if ( (boolean)newValue  //設定がOFF→ONになるとき
                  &&  Build.VERSION.SDK_INT >= 23 //Android 6.0以上のとき
                  &&  ContextCompat.checkSelfPermission(
                                SettingsFragment.this.getActivity(),
                                Manifest.permission.READ_EXTERNAL_STORAGE
                       )
                        != PackageManager.PERMISSION_GRANTED
                ) {
                    // パーミッション必要な理由を表示
                    toastIfShould(SettingsFragment.this);

                    // アクセス許可を要求（ダイアログを表示）
                    SettingsFragment.this.requestPermissions(
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            RQ_CODE_FROM_DIR
                    );
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
Log.d("○" + this.getClass().getSimpleName(), "onPreferenceClick() 呼ばれたdirPath");
                if ( Build.VERSION.SDK_INT >= 23
                  && ContextCompat.checkSelfPermission(
                        SettingsFragment.this.getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE
                     )
                       != PackageManager.PERMISSION_GRANTED
                ) {
                    // パーミッション必要な理由を表示するトーストが必要なときトーストを表示する
                    toastIfShould(SettingsFragment.this);

                    // アクセス許可を要求（ダイアログを表示）
                    SettingsFragment.this.requestPermissions(
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            RQ_CODE_FROM_DIR_PATH
                    );
                    return false;
                } else {
                    return true;
                }
            }
        });

        // ----------------------------------
        //
        // ----------------------------------
        return super.onCreateView(inflater, container, savedInstanceState);
    }
    /************************************
     * パーミッションがいる説明をトーストで表示する（表示しないといけない場合）
     */
    private void toastIfShould(SettingsFragment myThis) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                myThis.getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                ){
            Toast.makeText(myThis.getActivity(),
                    myThis.getString(R.string.permission_toast),
                    Toast.LENGTH_LONG)
                    .show();
        }
    }

    /************************************
     * インスタンス消失前のデータを保存するタイミング（画面回転直前）
     * @param outState このバンドルにデータを保存ずる
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);    //これ絶対呼ばないとダメ、selectDirのonSaveInstanceが呼ばれない
Log.d("○" + this.getClass().getSimpleName(), "onSaveInstanceState() 呼ばれた");
    }

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
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults
    ) {
Log.d("○"+this.getClass().getSimpleName(), "onRequestPermissionsResult():");
        switch (requestCode) {
            case RQ_CODE_FROM_DIR:
                // 許可をクリックしたとき
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // ディレクトリから の設定をONにする
                    ((SwitchPreference)this.findPreference(KEY_FROM_DIR)).setChecked(true);
                    // SharedPreferenceが変更したときのイベントを発火
                    this.onSharedPreferenceChanged(this.sp, KEY_FROM_DIR);
                }
                break;
            case RQ_CODE_FROM_DIR_PATH:
                // 許可をクリックしたとき
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // もう一度Preferenceをクリックする
                    ((SelectDirPreference)this.findPreference(KEY_FROM_DIR_PATH)).click();
                }
                break;
        }
    }

    /**
     * 設定変更したときのイベントハンドラ
     * @param sp SharedPreferences、保存された設定のオブジェクト
     * @param key 設定の値を取り出すためのkey, このkeyの設定が変更された
     */
    public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
Log.d("○△"+this.getClass().getSimpleName(), "onSharedPreferenceChanged(): key名:" + key);
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
        // Listの値を変更
        // ----------------------------------
        //// 開始時間
        if ( key.equals(KEY_WHEN_TIMER_INTERVAL) ) {
            ListPreference startTimePreference = ((ListPreference)this.findPreference(KEY_WHEN_TIMER_START_TIME));
            //index取得はsetStartTimePreferenceの前にすること
            int index = startTimePreference.findIndexOfValue( startTimePreference.getValue() );

            this.setStartTimePreference();

            startTimePreference.setValueIndex(index);
//            String sssss = this.sp.getString(KEY_WHEN_TIMER_START_TIME, "");
//            String fff = "ss";
        }


        // ----------------------------------
        // ボタンが切り替わったことをサービスに伝える
        // ----------------------------------
        this.mainService.onSPChanged(key);
        
    }

}




