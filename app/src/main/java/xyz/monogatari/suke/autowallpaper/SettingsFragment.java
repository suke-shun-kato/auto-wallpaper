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
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;
import java.util.Map;

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
    public static final String KEY_WHEN_SCREEN_ON = "when_turnOn";

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
Log.d("○" + this.getClass().getSimpleName(), "onCreate()が呼ばれた");
        super.onCreate(savedInstanceState);

        // 設定xmlを読み込む
        this.addPreferencesFromResource(R.xml.preferences);
    }

    /************************************
     * フラグメントが表示される直前
     */
    @Override
    public void onStart() {
Log.d("○" + this.getClass().getSimpleName(), "onStart()が呼ばれた");
        super.onStart();

        // ----------------------------------
        // サービスへバインドする
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
    /************************************
     * Preference.setOnPreferenceClickListener
     * フラグメントに関連づいたView層を生成する直前
     */
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // ----------------------------------
        // サマリーの表示の設定
        // ----------------------------------
        this.sp = PreferenceManager.getDefaultSharedPreferences( this.getActivity() );

        //// 選択ディレクトリ
        Preference keyFromDirPathPref = this.findPreference(KEY_FROM_DIR_PATH);
        String str = this.sp.getString(KEY_FROM_DIR_PATH, this.getString(R.string.setting_from_dir_which_default_summary) );


        keyFromDirPathPref.setSummary( str );

        // ----------------------------------
        // <Preference>のイベントリスナの設定、主にパーミッションダイアログ表示用
        // ----------------------------------
        // ----------
        // 取得元 < ディレクトリから のパーミッションダイアログ表示設定
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
                //// ONになるとき、かつストレージアクセスの許可を得ていないとき
                if (
                     ( (boolean)newValue )
                  && ( ContextCompat.checkSelfPermission(
                                SettingsFragment.this.getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED
                     )
                ) {

                    // アクセス許可を要求（ダイアログを表示）
                    if (Build.VERSION.SDK_INT >= 23) {  //Android 6.0以上のとき
                        SettingsFragment.this.requestPermissions(
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                RQ_CODE_FROM_DIR
                        );
                    }
                    return false;
                } else {
                    return true;
                }
            }
        });

        // ----------
        // 取得元 < ディレクトリを設定 のパーミッションダイアログ表示設定
        // ----------
        this.findPreference(KEY_FROM_DIR_PATH).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener(){
            @Override
            public boolean onPreferenceClick(Preference preference) {
Log.d("○" + this.getClass().getSimpleName(), "onPreferenceClick() 呼ばれたdirPath");
                return false;
            }
        });

        // ----------------------------------
        //
        // ----------------------------------
        return super.onCreateView(inflater, container, savedInstanceState);
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
        switch (requestCode) {
            case RQ_CODE_FROM_DIR:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // ディレクトリから の設定をONにする
                    ((SwitchPreference)this.findPreference(KEY_FROM_DIR)).setChecked(true);
                    // SharedPreferenceが変更したときのイベントを発火
                    this.onSharedPreferenceChanged(this.sp, KEY_FROM_DIR);
                }
                break;
            case RQ_CODE_FROM_DIR_PATH:
                break;
        }
    }

    /**
     * 設定変更したときのイベントハンドラ
     * @param sp SharedPreferences、保存された設定のオブジェクト
     * @param key 設定の値を取り出すためのkey, このkeyの設定が変更された
     */
    public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
Log.d("○"+this.getClass().getSimpleName(), "onSharedPreferenceChanged():");
        // ----------------------------------
        //
        // ----------------------------------
//        switch (key) {
//            case KEY_FROM_DIR:
//            case KEY_FROM_DIR_PATH:
//                // リクエストコードのマップを作成
//                Map<String, Integer> requestCodeMap = new HashMap<>();
//                requestCodeMap.put(KEY_FROM_DIR, RQ_CODE_FROM_DIR);
//                requestCodeMap.put(KEY_FROM_DIR_PATH, RQ_CODE_FROM_DIR_PATH);
//
//                //// ストレージアクセスの許可を得ていないとき
//                if (ContextCompat.checkSelfPermission(this.getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
//                        != PackageManager.PERMISSION_GRANTED) {
//
//                    // アクセス許可を要求（ダイアログを表示）
//                    ActivityCompat.requestPermissions(
//                            this.getActivity(),
//                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
//                            requestCodeMap.get(key)
//                    );
//                    return;
//                }
//                break;
//        }

        // ----------------------------------
        // 設定値をSummaryに反映
        // ----------------------------------
        // ----------
        // ディレクトリ選択
        // ----------
        if ( key.equals(KEY_FROM_DIR_PATH) ) {
            Preference fromDirPathPreference = this.findPreference(key);
            fromDirPathPreference.setSummary(sp.getString(key, ""));
        }

        // ----------------------------------
        // ボタンが切り替わったことをサービスに伝える
        // ----------------------------------
        this.mainService.onSPChanged(key);
        
    }

}




