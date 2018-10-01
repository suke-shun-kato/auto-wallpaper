package xyz.monogatari.autowallpaper;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.HashMap;
import java.util.Map;

import xyz.monogatari.autowallpaper.util.MySQLiteOpenHelper;

@SuppressWarnings("WeakerAccess")
public class HistoryModel {
    // --------------------------------------------------------------------
    //
    // --------------------------------------------------------------------
    private final SQLiteDatabase mDbReadable;
    private final SQLiteDatabase mDbWritable;

    public static final String TABLE_HISTORIES = "histories";
    public static final String[] HISTORIES_PROJECTION = new String[] {
            "_id",
            "source_kind",
            "img_uri",
            "intent_action_uri",
            "created_at"
    };

    public static final String SOURCE_TW = "ImgGetterTw";
    public static final String SOURCE_DIR = "ImgGetterDir";

    public static final Map<String, Integer> ICON_R_IDS = new HashMap<>();
    static {
        ICON_R_IDS.put(SOURCE_TW, R.drawable.ic_dir);
        ICON_R_IDS.put(SOURCE_DIR, R.drawable.ic_twitter);
    }

    // --------------------------------------------------------------------
    // 
    // --------------------------------------------------------------------
    HistoryModel(Context context) {
        MySQLiteOpenHelper dbHelper = MySQLiteOpenHelper.getInstance(context);
        mDbReadable = dbHelper.getReadableDatabase();
        mDbWritable = dbHelper.getWritableDatabase();

    }
    // --------------------------------------------------------------------
    // 
    // --------------------------------------------------------------------
    /**
     * idを指定して履歴データを取得する
     * @param id id
     * @return cursor
     */
    public Cursor getHistoryById(long id) {
        return mDbReadable.query(
                TABLE_HISTORIES,
                HISTORIES_PROJECTION,
                "_id=?",
                new String[] {String.valueOf(id)},
                null, null, null);
    }

    /**
     * 全ての履歴データを取得する
     * @return 履歴データ
     */
    public Cursor getAllHistories() {
        return mDbReadable.query(
                TABLE_HISTORIES,
                HISTORIES_PROJECTION,
                null, null, null, null,
                "created_at DESC",
                String.valueOf(HistoryActivity.MAX_RECORD_STORE));
    }

    /**
     * idを指定して削除
     * @param id 削除対象のid
     * @return 削除したレコード数
     */
    public int deleteHistories(long id) {
        return mDbWritable.delete(TABLE_HISTORIES, "_id = ?", new String[] {String.valueOf(id)});

    }
}
