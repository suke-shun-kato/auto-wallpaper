package xyz.monogatari.suke.autowallpaper.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by k-shunsuke on 2018/02/04.
 * データベースヘルパークラス
 */

public class FeedReaderDbHelper extends SQLiteOpenHelper {
    // --------------------------------------------------------------------
    //
    // --------------------------------------------------------------------
    // If you change the database schema, you must increment the database version.
    @SuppressWarnings("WeakerAccess")
    public static final int DATABASE_VERSION = 5;
    @SuppressWarnings("WeakerAccess")
    public static final String DATABASE_NAME = "master.sqlite3";

    
    // --------------------------------------------------------------------
    // 
    // --------------------------------------------------------------------
    public FeedReaderDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE histories ( " +
                "`id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                "`source_kind` INTEGER NOT NULL, " +
                "`img_uri` TEXT NOT NULL, " +
                "`intent_action_uri` TEXT, " +
                "`created_at` INTEGER NOT NULL )");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL("DROP TABLE IF EXISTS histories");
        onCreate(db);
    }


}