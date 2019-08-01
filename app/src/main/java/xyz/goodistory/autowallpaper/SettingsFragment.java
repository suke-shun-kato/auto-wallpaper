package xyz.goodistory.autowallpaper;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import xyz.goodistory.autowallpaper.preference.InstagramOAuthPreference;
import xyz.goodistory.autowallpaper.preference.SelectDirectoryPreferenceOld;
import xyz.goodistory.autowallpaper.preference.TwitterOAuthPreference;
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

    private SharedPreferences mSp;

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
    //// request code
    // TODO R.integer.permission_request_code_from_directory を使うようにする
    private static final int RQ_CODE_FROM_DIR = 1;

    //// preference key
    // TODO リソースから取得するようにする
    private String PREFERENCE_KEY_SELECT_DIRECTORY;
    private String PREFERENCE_KEY_FROM_DIR;
    private String PREFERENCE_KEY_FROM_TWITTER_FAVORITES;
    private String PREFERENCE_KEY_AUTHENTICATE_TWITTER;
    private String PREFERENCE_KEY_FROM_INSTAGRAM_USER_RECENT;// TODO インスタ復活したら使う
    private String PREFERENCE_KEY_AUTHENTICATE_INSTAGRAM;
    private String PREFERENCE_KEY_ABOUT;

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

        //// preference key の読み込み
        PREFERENCE_KEY_FROM_DIR = getString(R.string.preference_key_from_directory);
        PREFERENCE_KEY_SELECT_DIRECTORY = getString(R.string.preference_key_select_directory);

        PREFERENCE_KEY_FROM_TWITTER_FAVORITES
                = getString(R.string.preference_key_from_twitter_favorites);
        PREFERENCE_KEY_AUTHENTICATE_TWITTER
                = getString(R.string.preference_key_authenticate_twitter);

        PREFERENCE_KEY_FROM_INSTAGRAM_USER_RECENT
                = getString(R.string.preference_key_from_instagram_user_recent);
        PREFERENCE_KEY_AUTHENTICATE_INSTAGRAM
                = getString(R.string.preference_key_authenticate_instagram);

        PREFERENCE_KEY_ABOUT = getString(R.string.preference_key_about);

        //// 設定xmlを読み込む
        addPreferencesFromResource(R.xml.preferences);
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
        findPreference(PREFERENCE_KEY_ABOUT).setTitle(
                String.format( getString(R.string.setting_other_about_title), getString(R.string.app_name) )
        );


        // ----------------------------------
        // サマリーの表示の設定
        // ----------------------------------
        mSp = PreferenceManager.getDefaultSharedPreferences( getActivity() );

        //// 選択ディレクトリ
        String summaryText = mSp.getString(PREFERENCE_KEY_SELECT_DIRECTORY,
                getString(R.string.setting_from_dir_which_default_summary) );

        Preference fromDirPathPref = findPreference(PREFERENCE_KEY_SELECT_DIRECTORY);
        fromDirPathPref.setSummary( summaryText );

        //// Twitter認証
        TwitterOAuthPreference twitterPref
                = (TwitterOAuthPreference)findPreference(PREFERENCE_KEY_AUTHENTICATE_TWITTER);

        if ( twitterPref.hasAccessToken() ) {
            twitterPref.setSummary(R.string.setting_summary_oauth_done);
        } else {
            twitterPref.setSummary(R.string.setting_summary_oauth_notYet);
        }

        //// Instagram認
        // TODO api使用許可出たら復活
//        InstagramOAuthPreference instagramOAuthPreference
//                = (InstagramOAuthPreference)findPreference(PREFERENCE_KEY_AUTHENTICATE_INSTAGRAM);
//        // サマリーを更新
//        instagramOAuthPreference.updateSummary();


        // ----------------------------------
        // <Preference>のイベントリスナの設定、主にパーミッションダイアログ表示用
        // ----------------------------------
        // ----------
        // 「ディレクトリから」 のパーミッションダイアログ表示設定
        // ----------
        // ここは setOnPreferenceClickListener() ではない
        findPreference(PREFERENCE_KEY_FROM_DIR).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
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
        findPreference(PREFERENCE_KEY_FROM_TWITTER_FAVORITES)
                .setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            /************************************
             * @param preference クリックされたPreference
             * @param newValue Preferenceの新しい値
             * @return true:値変更を反映、false:反映しない
             */
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if ( mSp.getString(PREFERENCE_KEY_AUTHENTICATE_TWITTER, null) == null ) {
                    // Twitterの認証がまだのとき
                    Toast.makeText(getActivity(), R.string.setting_form_twitter_fav_error, Toast.LENGTH_SHORT).show();
                    return false;
                } else {
                    return true;
                }
            }
        });

        // ----------
        // Instagramの最近の投稿をクリックからしたとき
        // ----------
        // TODO api使用許可出たら復活
//        findPreference(PREFERENCE_KEY_FROM_INSTAGRAM_USER_RECENT).setOnPreferenceChangeListener(
//                new Preference.OnPreferenceChangeListener() {
//            /************************************
//             * @param preference クリックされたPreference
//             * @param newValue Preferenceの新しい値
//             * @return true:値変更を反映、false:反映しない
//             */
//            @Override
//            public boolean onPreferenceChange(Preference preference, Object newValue) {
//                if ( mSp.getString(PREFERENCE_KEY_AUTHENTICATE_INSTAGRAM, null) == null ) {
//                    Toast.makeText(getActivity(),
//                            R.string.preference_error_msg_no_authorize,
//                            Toast.LENGTH_LONG)
//                            .show();
//                    return false;
//                } else {
//                    return true;
//                }
//            }
//        });



        // ----------
        //
        // ----------
        findPreference(PREFERENCE_KEY_ABOUT).setOnPreferenceClickListener(
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
        // その他の設定
        // ----------------------------------
//        TODO api使用許可出たら復活
//        if (Build.VERSION.SDK_INT < InstagramOAuthPreference.SUPPORTED_API_LEVEL) {
//            findPreference(keyFromInstagram).setEnabled(false);
//        }

        // ----------------------------------
        //
        // ----------------------------------
        return super.onCreateView(inflater, container, savedInstanceState);
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
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions,  @NonNull int[] grantResults) {

        // SelectDirectoryPreferenceOld での onRequestPermissionsResult() を実行
        ((SelectDirectoryPreferenceOld)findPreference(PREFERENCE_KEY_SELECT_DIRECTORY))
                .onRequestPermissionsResult(requestCode, permissions, grantResults);

        // TODO ↑のようにする
        switch (requestCode) {
            case RQ_CODE_FROM_DIR:
                // 許可をクリックしたとき
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                    // ディレクトリから の設定をONにする
                    ((SwitchPreference) findPreference(PREFERENCE_KEY_FROM_DIR)).setChecked(true);
                    // SharedPreferenceが変更したときのイベントを発火
                    this.onSharedPreferenceChanged(mSp, PREFERENCE_KEY_FROM_DIR);
                }
                break;
        }
    }

    /**
     *
     * 設定変更したときのイベントハンドラ、OnSharedPreferenceChangeListenerのメソッド
     * @param sp SharedPreferences、保存された設定のオブジェクト
     * @param preferenceKey 設定の値を取り出すためのkey, このkeyの設定が変更された
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sp, String preferenceKey) {

        // ----------------------------------
        // 設定値をSummaryに反映
        // ----------------------------------
        //// 反映
        if ( preferenceKey.equals(PREFERENCE_KEY_SELECT_DIRECTORY) ) {  //// ディレクトリ選択
            Preference fromDirPathPreference = findPreference(preferenceKey);
            fromDirPathPreference.setSummary(sp.getString(preferenceKey, ""));

        } else if ( preferenceKey.equals(PREFERENCE_KEY_AUTHENTICATE_TWITTER) ) {  //// Twitter認証
            Preference fromTwitterOauthPreference = findPreference(preferenceKey);
            fromTwitterOauthPreference.setSummary(R.string.setting_summary_oauth_done);

        } else if ( preferenceKey.equals(PREFERENCE_KEY_AUTHENTICATE_INSTAGRAM) ) {
        //// インスタグラム認証
            ((InstagramOAuthPreference)findPreference(preferenceKey)).updateSummary();
        }

        // ----------------------------------
        // ボタンが切り替わったことをサービスに伝える
        // ----------------------------------
        if (this.isServiceRunning) {
            this.mainService.onSPChanged(preferenceKey);
        }

    }

    // --------------------------------------------------------------------
    //
    // --------------------------------------------------------------------
    /**
     * SharedPreferenceに保存したキー
     * @param context コンテキスト
     * @return アクセストークン
     */
    @Nullable
    public static String getInstagramAccessToken(Context context) {
        String prefKey = context.getString(R.string.preference_key_authenticate_instagram);

        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(prefKey, null);
    }

    public static String getInstagramClientID(Context context) {
        return context.getString(R.string.instagram_client_id);
    }

}



