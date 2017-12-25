package xyz.monogatari.suke.autowallpaper;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;


/**
 * 設定画面のアクティビティ
 * Created by k-shunsuke on 2017/12/08.
 */
public class SettingsActivity extends AppCompatActivity {
    // --------------------------------------------------------------------
    // フィールド
    // --------------------------------------------------------------------


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
Log.d("○" + this.getClass().getSimpleName(), "onCreate() 呼ばれた: ");
        super.onCreate(savedInstanceState);
Log.d("○" + this.getClass().getSimpleName(), "onCreate() 呼ばれた: super2");
        if ( savedInstanceState == null) {
            this.getFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new SettingsFragment())
                    .commit();
            Log.d("○" + this.getClass().getSimpleName(), "onCreate() 呼ばれた: super3");
        }

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
     * @param intent
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // 新しいIntentが来た場合Fragmentで新しいIntentを取得するためにセットする
        // ここはTwitter認証の為に更新している
        this.setIntent(intent);
    }

    /************************************
     *
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
Log.d("○" + this.getClass().getSimpleName(), "onSaveInstanceState() 呼ばれた");
        super.onSaveInstanceState(outState);
    }
}
