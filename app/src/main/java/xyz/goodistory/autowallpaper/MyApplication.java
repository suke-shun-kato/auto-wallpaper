package xyz.goodistory.autowallpaper;

import android.app.Application;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

@SuppressWarnings("WeakerAccess")
public class MyApplication extends Application {
    /**
     * Called when the application is starting, before any activity, service,
     * or receiver objects (excluding content providers) have been created.
     * Implementations should be as quick as possible (for example using
     * lazy initialization of state) since the time spent in this function
     * directly impacts the performance of starting the first activity,
     * service, or receiver in a process.
     * If you override this method, be sure to call super.onCreate().
     */
    @Override
    public void onCreate() {
        super.onCreate();

        // ----------------------------------
        // ImageLoader の設定
        // ----------------------------------
        //// displayImage() 関数の設定
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                // ダウンロード中の表示画像
                .showImageOnLoading(R.drawable.anim_refresh)
                // URLが空だったときの表示画像
                .showImageForEmptyUri(R.drawable.ic_history_remove)
                // ネット未接続やURLが間違っていて失敗したときの表示画像
                .showImageOnFail(R.drawable.ic_history_error)
                // メモリにキャッシュを有効
                .cacheInMemory(true)
//           .cacheOnDisk(true)
                .build();

        //// imageLoader自体の設定
        ImageLoaderConfiguration config
                = new ImageLoaderConfiguration.Builder(this.getApplicationContext())
                .defaultDisplayImageOptions(defaultOptions)
                .memoryCacheSizePercentage(25)
                .build();
        ImageLoader.getInstance().init(config);
    }
}
