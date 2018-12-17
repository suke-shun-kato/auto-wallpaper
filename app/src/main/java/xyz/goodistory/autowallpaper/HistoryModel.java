package xyz.goodistory.autowallpaper;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import xyz.goodistory.autowallpaper.util.MySQLiteOpenHelper;
import xyz.goodistory.autowallpaper.wpchange.ImgGetter;

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
    public static final String SOURCE_SHARE = "share";

    public static final Map<String, Integer> ICON_R_IDS = new HashMap<>();
    static {
        ICON_R_IDS.put(SOURCE_DIR, R.drawable.ic_dir);
        ICON_R_IDS.put(SOURCE_TW, R.drawable.ic_twitter);
        ICON_R_IDS.put(SOURCE_SHARE, R.drawable.ic_share);
    }

    // --------------------------------------------------------------------
    // 
    // --------------------------------------------------------------------
    public HistoryModel(Context context) {
        MySQLiteOpenHelper dbHelper = MySQLiteOpenHelper.getInstance(context);
        mDbReadable = dbHelper.getReadableDatabase();
        mDbWritable = dbHelper.getWritableDatabase();

    }

    public void close() {
        mDbReadable.close();
        mDbWritable.close();
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
     * 壁紙の履歴をDBに登録する
     * @param imgGetter 登録対象
     */
    public void insertHistories(ImgGetter imgGetter) {
        // ----------------------------------
        // INSERT
        // ----------------------------------
        //// コード準備

        SQLiteStatement dbStt = mDbWritable.compileStatement("" +
                "INSERT INTO histories (" +
                "source_kind, img_uri, intent_action_uri, created_at" +
                ") VALUES ( ?, ?, ?, datetime('now') );");

        //// bind
        dbStt.bindString(1, imgGetter.getSourceKind() );
        dbStt.bindString(2, imgGetter.getImgUri());
        String uri = imgGetter.getActionUri();
        if (uri == null) {
            dbStt.bindNull(3);
        } else {
            dbStt.bindString(3, imgGetter.getActionUri());
        }

        //// insert実行
        dbStt.executeInsert();

    }



    /**
     * idを指定して削除
     * @param id 削除対象のid
     * @return 削除したレコード数
     */
    public int deleteHistories(long id) {
        return mDbWritable.delete(TABLE_HISTORIES, "_id = ?", new String[] {String.valueOf(id)});

    }

    /**
     * 古いものを削除
     * @param maxNum 残しておく最大値
     */
    public void deleteHistoriesOverflowMax(int maxNum) {
        Cursor cursor = mDbWritable.rawQuery(
                "SELECT count(*) AS count FROM histories", null);
        if (cursor == null) {
            return;
        }

        if ( cursor.moveToFirst()) {
            int recordCount = cursor.getInt(cursor.getColumnIndexOrThrow("count"));

            if (recordCount > maxNum) {
                SQLiteStatement dbStt = mDbWritable.compileStatement(
                        "DELETE FROM histories WHERE created_at IN (" +
                                "SELECT created_at FROM histories ORDER BY created_at ASC LIMIT ?)"
                );
                dbStt.bindLong(1, recordCount - maxNum);
                dbStt.executeUpdateDelete();
            }
        }
        cursor.close();
    }

    /**
     * 日時を yyyy-mm-dd hh:mm:ss 形式からUnixTimeに変換（UTC）
     * @param yyyymmddhhmmss 変換したい日時
     * @return UnixTime(millisecond)
     * @throws ParseException 変換失敗時例外を投げる
     */
    public static long sqliteToUnixTimeMillis(String yyyymmddhhmmss) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        sdf.setTimeZone( TimeZone.getTimeZone("UTC") );
        Date date = sdf.parse(yyyymmddhhmmss);
        return date.getTime();
    }
}
