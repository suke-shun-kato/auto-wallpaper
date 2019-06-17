package xyz.goodistory.autowallpaper;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import xyz.goodistory.autowallpaper.preference.TwitterOAuthPreference;


/**
 * 設定画面のアクティビティ
 * Created by k-shunsuke on 2017/12/08.
 */
public class SettingsActivity extends AppCompatActivity {
    // --------------------------------------------------------------------
    // フィールド
    // --------------------------------------------------------------------
    private SettingsFragment settingFragment;


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
        this.settingFragment.onRequestPermissionsResultFragment(requestCode, permissions, grantResults);

    }



    /************************************
     * アクティビティが作成されたとき
     * @param savedInstanceState
     */
    @SuppressWarnings("JavaDoc")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // ----------------------------------
        // 
        // ----------------------------------
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_settings);


        // ----------------------------------
        // アクションバーの設定
        // ----------------------------------
        //// ツールバーをアクションバーとして表示
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        this.setSupportActionBar(myToolbar);

        //// アクションバーに「←」ボタンを表示
        // 詳しくはHistoryActivity.javaを参照
        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // ----------------------------------
        //
        // ----------------------------------
        this.settingFragment = (SettingsFragment)this.getFragmentManager().findFragmentById(R.id.setting_fragment);

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
