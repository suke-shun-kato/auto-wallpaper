package xyz.monogatari.suke.autowallpaper;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import xyz.monogatari.suke.autowallpaper.service.MainService;

/**
 * 設定画面のフラグメント、
 * サービスへのバインドはフラグメントで行う方が良い（違うアクティビティにアタッチされるかもしれないので）
 * Created by k-shunsuke on 2017/12/08.
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{
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
    private static final String KEY_FROM_DIR_PATH = "from_dir_path";

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
     *
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
     *
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
     *
     */
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences( this.getActivity() );

        //// 選択ディレクトリ
        Preference keyFromDirPathPref = this.findPreference(KEY_FROM_DIR_PATH);
        String str = sp.getString(KEY_FROM_DIR_PATH, this.getString(R.string.setting_from_dir_which_default_summary) );


        keyFromDirPathPref.setSummary( str );

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    /************************************
     *
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
     * フラグメントが利用可能状態になったとき、アク
     * ティビティがフォアグラウンドになるとき
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
    /**
     * 設定変更したときのイベントハンドラ
     * @param sp SharedPreferences、保存された設定のオブジェクト
     * @param key 設定の値を取り出すためのkey, このkeyの設定が変更された
     */
    public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
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
        // メイン処理、押されたボタンによってサービスを操作
        // ----------------------------------
        
    }

}




