package xyz.monogatari.suke.autowallpaper;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;

/**
 * 設定画面のフラグメント
 * Created by k-shunsuke on 2017/12/08.
 */
public class SettingsFragment extends PreferenceFragment {
    /************************************
     * フラグメントが作成されたとき
     * @param savedInstanceState
     */
    @SuppressWarnings("JavaDoc")
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 設定xmlを読み込む
        this.addPreferencesFromResource(R.xml.preferences);
    }
}
