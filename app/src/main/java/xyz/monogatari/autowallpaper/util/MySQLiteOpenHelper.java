package xyz.monogatari.autowallpaper.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by k-shunsuke on 2018/02/04.
 * データベースヘルパークラス
 */

public class MySQLiteOpenHelper extends SQLiteOpenHelper {



    // --------------------------------------------------------------------
    //
    // --------------------------------------------------------------------
    // If you change the database schema, you must increment the database version.
    @SuppressWarnings("WeakerAccess")
    public static final int DATABASE_VERSION = 14;
    @SuppressWarnings("WeakerAccess")
    public static final String DATABASE_NAME = "master.sqlite3";

    private static MySQLiteOpenHelper sMySQLiteOpenHelper = null;

    
    // --------------------------------------------------------------------
    // 
    // --------------------------------------------------------------------
    private MySQLiteOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized MySQLiteOpenHelper getInstance(Context context) {
        if (sMySQLiteOpenHelper == null) {
            sMySQLiteOpenHelper = new MySQLiteOpenHelper(context);
        }
        return sMySQLiteOpenHelper;
    }

    // --------------------------------------------------------------------
    //
    // --------------------------------------------------------------------

    /**
     * TODO id → _id にする
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // デーブル作成
        db.execSQL("CREATE TABLE histories ( " +
                "`id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                "`source_kind` TEXT NOT NULL, " +
                "`img_uri` TEXT NOT NULL, " +
                "`intent_action_uri` TEXT, " +
                "`created_at` TEXT NOT NULL )");
        // インデックス作成
        db.execSQL("CREATE INDEX created_at ON histories(created_at)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL("DROP TABLE IF EXISTS histories");
        onCreate(db);
    }


}