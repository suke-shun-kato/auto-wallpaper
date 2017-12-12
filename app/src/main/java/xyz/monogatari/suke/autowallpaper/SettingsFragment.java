package xyz.monogatari.suke.autowallpaper;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * 設定画面のフラグメント
 * Created by k-shunsuke on 2017/12/08.
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{
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
    }

}




