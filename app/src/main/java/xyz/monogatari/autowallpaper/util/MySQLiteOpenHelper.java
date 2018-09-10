package xyz.monogatari.autowallpaper.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

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
    public static final int DATABASE_VERSION = 26;
    @SuppressWarnings("WeakerAccess")
    public static final String DATABASE_NAME = "master.sqlite3";

    private static MySQLiteOpenHelper sMySQLiteOpenHelper = null;


    public static final String TABLE_HISTORIES = "histories";
    public static final String[] HISTORIES_PROJECTION = new String[] {
            "_id",
            "source_kind",
            "img_uri",
            "intent_action_uri",
            "created_at"
    };
    
    // --------------------------------------------------------------------
    // コンストラクタ、シングルトンにする
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
    @Override
    public void onCreate(SQLiteDatabase db) {
        // デーブル作成
        db.execSQL("CREATE TABLE histories ( " +
                "`_id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                "`source_kind` TEXT NOT NULL, " +
                "`img_uri` TEXT NOT NULL, " +
                "`intent_action_uri` TEXT, " +
                "`created_at` TEXT NOT NULL )");
        // インデックス作成
        db.execSQL("CREATE INDEX created_at ON histories(created_at)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //// 1～13 → 14
        if (oldVersion <= 13 && newVersion >= 14) {
            db.execSQL("DROP TABLE IF EXISTS histories");
            onCreate(db);
        }

        //// 15～25 → 26
        if (oldVersion <= 25 && newVersion >= 26) {
            //// id → _id カラムに変更
            db.execSQL("DROP TABLE IF EXISTS histories_temp");
            db.execSQL("ALTER TABLE histories RENAME TO histories_temp");

            db.execSQL("CREATE TABLE histories (" +
                    "`_id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    "`source_kind` TEXT NOT NULL, " +
                    "`img_uri` TEXT NOT NULL, " +
                    "`intent_action_uri` TEXT, " +
                    "`created_at` TEXT NOT NULL )");
            db.execSQL("CREATE INDEX created_at ON histories(created_at)");


            db.execSQL("INSERT INTO histories(source_kind, img_uri, intent_action_uri, created_at) " +
                    "SELECT source_kind, img_uri, intent_action_uri, created_at FROM histories_temp");

            db.execSQL("DROP TABLE histories_temp");
        }
    }


}