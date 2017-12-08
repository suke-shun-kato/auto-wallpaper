package xyz.monogatari.suke.autowallpaper;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * 設定画面のアクティビティ
 * Created by k-shunsuke on 2017/12/08.
 */
public class SettingsActivity extends AppCompatActivity {
    /************************************
     * アクティビティが作成されたとき
     * @param savedInstanceState
     */
    @SuppressWarnings("JavaDoc")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}
