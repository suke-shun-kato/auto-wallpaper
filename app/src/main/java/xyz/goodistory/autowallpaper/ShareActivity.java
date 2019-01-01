package xyz.goodistory.autowallpaper;

import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import xyz.goodistory.autowallpaper.wpchange.WpManagerService;


public class ShareActivity extends AppCompatActivity {
    /************************************
     * アクティビティ作成時
     * @param savedInstanceState 画像回転時などにアクティビティ破棄時に保存した値
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // ----------------------------------
        //
        // ----------------------------------
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_share);

        // ----------------------------------
        // アクションバーの設定
        // ----------------------------------
        //// ツールバーをアクションバーとして表示
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        this.setSupportActionBar(myToolbar);

        // ----------------------------------
        //
        // ----------------------------------
        Intent intent = getIntent();
        if (intent != null) {
            String action = intent.getAction();
            String type = intent.getType();
            Bundle bundle = intent.getExtras();
            if ( action != null
                    && action.equals(Intent.ACTION_SEND)
                    && type != null
                    && type.contains("image/")
                    && bundle != null
                    && bundle.get(Intent.EXTRA_STREAM) != null)  {
                // ----------------------------------
                // 共有からintentが来たときの処理、壁紙のセットするかダイアログを表示
                // ----------------------------------
                ShareImageFragment f = new ShareImageFragment();
                f.show(getSupportFragmentManager(), ShareImageFragment.TAG);


                // ----------------------------------
                // 背景をシェアした画像に設定
                // ----------------------------------
                //// 読み込み
                ImageView imageView = findViewById(R.id.share_image);
                final Object uriStr = bundle.get(Intent.EXTRA_STREAM);
                if (uriStr == null) {
                    throw new IllegalStateException("EXTRA_STREAM is null!");
                }

                ImageLoader imgLoader = ImageLoader.getInstance();
                imgLoader.displayImage(uriStr.toString(), imageView);

            } else {
                Toast.makeText(this, R.string.share_error_message, Toast.LENGTH_LONG)
                        .show();
            }
        } else {
            Toast.makeText(this, R.string.share_error_message, Toast.LENGTH_LONG)
                    .show();
        }
    }
}
