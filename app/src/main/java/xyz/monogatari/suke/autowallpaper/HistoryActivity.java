package xyz.monogatari.suke.autowallpaper;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import xyz.monogatari.suke.autowallpaper.util.MySQLiteOpenHelper;
import xyz.monogatari.suke.autowallpaper.wpchange.HistoryListAdapter;
import xyz.monogatari.suke.autowallpaper.wpchange.HistoryListItem;

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

        this.mDbHelper = new MySQLiteOpenHelper(this);

        // ----------------------------------
        //
        // ----------------------------------
Log.d("○□□□□□□□"+this.getClass().getSimpleName(), "onCreate()2");
        List<HistoryListItem> itemList = this.selectHistories();

        ListView lv = (ListView) this.findViewById(R.id.history_list);
        HistoryListAdapter adapter = new HistoryListAdapter(this, itemList, R.layout.history_list_item);
        lv.setAdapter(adapter);
Log.d("○□□□□□□□"+this.getClass().getSimpleName(), "onCreate()のend");

    }

    // --------------------------------------------------------------------
    // メソッド
    // --------------------------------------------------------------------
    private List<HistoryListItem> selectHistories() {
        SQLiteDatabase db = this.mDbHelper.getReadableDatabase();
        Cursor cursor = null;

        //noinspection TryFinallyCanBeTryWithResources
        try {
            cursor = db.rawQuery("SELECT *, datetime(created_at, 'localtime') AS created_at_local  FROM histories ORDER BY created_at DESC", null);

            List<HistoryListItem> itemList = new ArrayList<>();
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    HistoryListItem item = new HistoryListItem(
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
