package xyz.monogatari.suke.autowallpaper;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;

import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;
import java.util.List;

import xyz.monogatari.suke.autowallpaper.util.MySQLiteOpenHelper;

/**
 * 履歴ページ、ひとまず作成
 * Created by k-shunsuke on 2017/12/20.
 */

public class HistoryActivity extends AppCompatActivity {
    // --------------------------------------------------------------------
    //
    // --------------------------------------------------------------------
    private MySQLiteOpenHelper mDbHelper;

    // --------------------------------------------------------------------
    // メソッド（オーバーライド）
    // --------------------------------------------------------------------
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
Log.d("○□□□□□□□"+this.getClass().getSimpleName(), "onCreate()のstart");
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_history);

        // ----------------------------------
        //
        // ----------------------------------
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
            // ダウンロード中の表示画像
            .showImageOnLoading(R.drawable.ic_arrow_downward_black_24dp)
            // URLが空だったときの表示画像
            .showImageForEmptyUri(R.drawable.ic_do_not_disturb_black_24dp)
            // ネット未接続やURLが間違っていて失敗したときの表示画像
            .showImageOnFail(R.drawable.ic_do_not_disturb_black_24dp)
            // メモリにキャッシュを有効
            .cacheInMemory(true)
//           .cacheOnDisk(true)
            .build();


        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this.getApplicationContext())
            .defaultDisplayImageOptions(defaultOptions)
            .build();
        ImageLoader.getInstance().init(config);

        // ----------------------------------
        //
        // ----------------------------------
        this.mDbHelper = new MySQLiteOpenHelper(this);
Log.d("○□□□□□□□"+this.getClass().getSimpleName(), "onCreate()2");
        List<HistoryItemListDataStore> itemList = this.selectHistories();

        ListView lv = (ListView) this.findViewById(R.id.history_list);
        HistoryListAdapter adapter = new HistoryListAdapter(this, itemList, R.layout.item_list_history);
        lv.setAdapter(adapter);
Log.d("○□□□□□□□"+this.getClass().getSimpleName(), "onCreate()のend");

    }

    // --------------------------------------------------------------------
    // メソッド
    // --------------------------------------------------------------------
    private List<HistoryItemListDataStore> selectHistories() {
        SQLiteDatabase db = this.mDbHelper.getReadableDatabase();
        Cursor cursor = null;

        //noinspection TryFinallyCanBeTryWithResources
        try {
            cursor = db.rawQuery("SELECT *, datetime(created_at, 'localtime') AS created_at_local  FROM histories ORDER BY created_at DESC", null);

            List<HistoryItemListDataStore> itemList = new ArrayList<>();
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    HistoryItemListDataStore item = new HistoryItemListDataStore(
                            cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("source_kind")),
                            cursor.getString(cursor.getColumnIndexOrThrow("img_uri")),
                            cursor.getString(cursor.getColumnIndexOrThrow("intent_action_uri")),
                            cursor.getString(cursor.getColumnIndexOrThrow("created_at_local"))
                    );
                    itemList.add(item);
                }
            }
            return itemList;
        } finally {
            cursor.close();
            db.close();
        }

    }
}
