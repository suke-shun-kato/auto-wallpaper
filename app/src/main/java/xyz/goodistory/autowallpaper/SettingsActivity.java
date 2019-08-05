package xyz.goodistory.autowallpaper;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import xyz.goodistory.autowallpaper.preference.TwitterOAuthPreference;


/**
 * 設定画面のアクティビティ
 * Created by k-shunsuke on 2017/12/08.
 */
public class SettingsActivity
        extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback {

    // --------------------------------------------------------------------
    // フィールド
    // --------------------------------------------------------------------
    private SettingsPreferenceFragment mSettingPreferenceFragment;


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
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        mSettingPreferenceFragment.onRequestPermissionsResult(
                requestCode, permissions, grantResults);

    }



    /************************************
     * アクティビティが作成されたとき
     * @param savedInstanceState 開店前などに保存した値
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // ----------------------------------
        // 基本処理
        // ----------------------------------
        super.onCreate(savedInstanceState);

        // レイアウトXMLから画面を作成する
        // SettingsPreferenceFragment はXMLのname属性で定義しているで、
        // ↓下記のようなコードで作成する必要はない
        // getSupportFragmentManager().beginTransaction().replace().commit();
        setContentView(R.layout.activity_settings);

        mSettingPreferenceFragment = (SettingsPreferenceFragment) getSupportFragmentManager()
                .findFragmentById(R.id.setting_preference_fragment);

        // ----------------------------------
        // アクションバーの設定
        // ----------------------------------
        //// ツールバーをアクションバーとして表示
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        //// アクションバーに「←」ボタンを表示
        // 詳しくはHistoryActivity.javaを参照
        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     *
     * Twitterの認証ボタン押下後のコールバック用として作成した
     * @param intent インテント
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        TwitterOAuthPreference.sendToAccessTokenBroadcast(intent, this);
    }

    /************************************
     *
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

}
