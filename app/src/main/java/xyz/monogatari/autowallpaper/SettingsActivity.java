package xyz.monogatari.autowallpaper;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;


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
Log.d("○_"+this.getClass().getSimpleName(), "onRequestPermissionsResult():");
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
Log.d("○" + this.getClass().getSimpleName(), "onCreate() 呼ばれた: ");
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_settings);
Log.d("○" + this.getClass().getSimpleName(), "onCreate() 呼ばれた: super2");


        // ----------------------------------
        // アクションバーの設定
        // ----------------------------------
        ////　ツールバーをアクションバーとして表示
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

Log.d("○" + this.getClass().getSimpleName(), "onCreate() 呼ばれた: super3");
    }

    @Override
    protected void onStart() {
Log.d("○" + this.getClass().getSimpleName(), "onStart() 呼ばれた: top");
        super.onStart();
Log.d("○" + this.getClass().getSimpleName(), "onStart() 呼ばれた: end");
    }

    /**
     * Handle onNewIntent() to inform the fragment manager that the
     * state is not saved.  If you are handling new intents and may be
     * making changes to the fragment state, you want to be sure to call
     * through to the super-class here first.  Otherwise, if your state
     * is saved but the activity is not stopped, you could get an
     * onNewIntent() call which happens before onResume() and trying to
     * perform fragment operations at that point will throw IllegalStateException
     * because the fragment manager thinks the state is still saved.
     *
     * Twitterの認証ボタン押下後のコールバック用として作成した
     * @param intent インテント
     */
    @Override
    protected void onNewIntent(Intent intent) {
Log.d("○" + this.getClass().getSimpleName(), "onNewIntent(): data: "+intent.getData());
        super.onNewIntent(intent);

        this.settingFragment.onNewIntent(intent);
    }

    /************************************
     *
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
Log.d("○" + this.getClass().getSimpleName(), "onSaveInstanceState() 呼ばれた");
        super.onSaveInstanceState(outState);
    }

//
//    /************************************
//     * オプションんのハンドラ
//     * 戻るボタンを押したときにアクティビティを終了する（ホームに戻る）ようにしている
//     * @param item 選択されたmenuアイテム
//     */
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case android.R.id.home:
//                this.finish();
//                return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
}
