package xyz.monogatari.suke.autowallpaper;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
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
Log.d("○" + this.getClass().getSimpleName(), "onCreate() 呼ばれた: super2");
 
        // ----------------------------------
        // アクションバーに戻るボタンを設置
        // ----------------------------------
        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // ----------------------------------
        // フラグメントをアクティビティにセット
        // 画面回転の場合（savedInstanceStateがnullでない場合）は自動的にセットされるのでここを通らないようにする
        // ----------------------------------
        if (savedInstanceState == null) {
            this.settingFragment = new SettingsFragment();
            this.getFragmentManager().beginTransaction()
                    .replace(android.R.id.content, this.settingFragment)
                    .commit();
        }
Log.d("○" + this.getClass().getSimpleName(), "onCreate() 呼ばれた: super3");
    }

    @Override
    protected void onStart() {
Log.d("○○○○○○○○" + this.getClass().getSimpleName(), "onStart() 呼ばれた: top");
        super.onStart();
Log.d("○○○○○○○○" + this.getClass().getSimpleName(), "onStart() 呼ばれた: end");
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
Log.d("○○○○○○○○○○" + this.getClass().getSimpleName(), "onNewIntent(): data: "+intent.getData());
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


    /************************************
     * オプションんのハンドラ
     * 戻るボタンを押したときにアクティビティを終了する（ホームに戻る）ようにしている
     * @param item 選択されたmenuアイテム
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
