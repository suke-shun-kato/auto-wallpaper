package xyz.monogatari.suke.autowallpaper;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;

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
        // 画像ローダーの初期設定
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
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this.getApplicationContext())
            .defaultDisplayImageOptions(defaultOptions)
            .memoryCacheSizePercentage(25)
            .build();
        ImageLoader.getInstance().init(config);

        // ----------------------------------
        //
        // ----------------------------------
        this.mDbHelper = new MySQLiteOpenHelper(this);
Log.d("○□□□□□□□"+this.getClass().getSimpleName(), "onCreate()2");
        List<HistoryItemListDataStore> itemList = this.selectHistories();

        ListView lv = this.findViewById(R.id.history_list);
        HistoryListAdapter adapter = new HistoryListAdapter(this, itemList, R.layout.item_list_history);
        lv.setAdapter(adapter);
Log.d("○□□□□□□□"+this.getClass().getSimpleName(), "onCreate()のend");


        // ----------------------------------
        //
        // ----------------------------------
//        Log.d("○○○",Environment.getExternalStorageDirectory().getAbsolutePath());
    }

    // --------------------------------------------------------------------
    // メソッド
    // --------------------------------------------------------------------
    private List<HistoryItemListDataStore> selectHistories() {
        SQLiteDatabase db = this.mDbHelper.getReadableDatabase();
        Cursor cursor = null;

        //noinspection TryFinallyCanBeTryWithResources
        try {
            // created_atの取得自体はUTCタイムでUNIXタイムスタンプのまま取得するので'utc'を追加
            cursor = db.rawQuery("SELECT id, source_kind, img_uri, intent_action_uri, strftime('%s', created_at, 'utc') AS created_at_unix FROM histories ORDER BY created_at DESC;", null);

            List<HistoryItemListDataStore> itemList = new ArrayList<>();
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    HistoryItemListDataStore item = new HistoryItemListDataStore(
                            cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                            cursor.getString(cursor.getColumnIndexOrThrow("source_kind")),
                            cursor.getString(cursor.getColumnIndexOrThrow("img_uri")),
                            cursor.getString(cursor.getColumnIndexOrThrow("intent_action_uri")),
                            (long)cursor.getInt(cursor.getColumnIndexOrThrow("created_at_unix"))*1000
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
