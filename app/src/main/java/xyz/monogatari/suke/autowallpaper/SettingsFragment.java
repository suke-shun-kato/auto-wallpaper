package xyz.monogatari.suke.autowallpaper;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * 設定画面のフラグメント
 * Created by k-shunsuke on 2017/12/08.
 */
public class SettingsFragment extends PreferenceFragment {
    /************************************
     * フラグメントが作成されたとき
     * @param savedInstanceState アクティビティ破棄時に保存した値
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
if (savedInstanceState == null) {
Log.d("○" + this.getClass().getSimpleName(), "onCreate()が呼ばれた null");

} else {
Log.d("○" + this.getClass().getSimpleName(), "onCreate()が呼ばれた not null");

}

        super.onCreate(savedInstanceState);
Log.d("○" + this.getClass().getSimpleName(), "onCreate()が呼ばれた2");

        // 設定xmlを読み込む
        this.addPreferencesFromResource(R.xml.preferences);
    }

    /************************************
     *
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);    //これ絶対呼ばないとダメ、selectDirのonSaveInstanceが呼ばれない
        outState.putString("aaaaaaaaaaaaaa", "dataContext");
Log.d("○" + this.getClass().getSimpleName(), "onSaveInstanceState() 呼ばれた");
    }
}




