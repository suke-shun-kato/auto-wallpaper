package xyz.goodistory.autowallpaper.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import xyz.goodistory.autowallpaper.service.ScreenOffHistoriesModel;

/**
 * Created by k-shunsuke on 2018/02/04.
 * データベースヘルパークラス
 */

public class MySQLiteOpenHelper extends SQLiteOpenHelper {
    // --------------------------------------------------------------------
    //
    // --------------------------------------------------------------------
    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 29;

    /** デバッグ用のデータベースのバージョン、onUpgrade()のoldVersionの値になる */
    private static final Integer DATABASE_OLD_VERSION_FOR_DEBUG = null;

    private static final String DATABASE_NAME = "master.sqlite3";

    private static MySQLiteOpenHelper sMySQLiteOpenHelper = null;

    
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
    // オーバーライド
    // --------------------------------------------------------------------
    /**
     * @param db The database.
     */
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);

        //// デバッグ用にバージョンをセットする、onUpgrade()のoldVersionの値になる
        if (DATABASE_OLD_VERSION_FOR_DEBUG != null) {
            db.setVersion(DATABASE_OLD_VERSION_FOR_DEBUG);
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // デーブル作成
        db.execSQL("DROP TABLE IF EXISTS histories");
        db.execSQL("CREATE TABLE histories ( " +
                "`_id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                "`source_kind` TEXT NOT NULL, " +
                "`img_uri` TEXT NOT NULL, " +
                "`intent_action_uri` TEXT, " +
                "`created_at` TEXT NOT NULL, " +
                "`device_img_uri` TEXT NOT NULL " +

                ")");
        // インデックス作成
        db.execSQL("CREATE INDEX created_at ON histories(created_at)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //// 1～13 → 全てのバージョン
        if (oldVersion <= 13) {
            onCreate(db);
            return;
        }

        //// 14～25 → 26
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
            // 同じ名前のインデックスを作成したらエラーがでるので、
            // その前に変更前のテーブルのインデックスを削除
            db.execSQL("DROP INDEX IF EXISTS created_at");
            db.execSQL("CREATE INDEX created_at ON histories(created_at)");

            db.execSQL("INSERT INTO histories(source_kind, img_uri, intent_action_uri, created_at) " +
                    "SELECT source_kind, img_uri, intent_action_uri, created_at FROM histories_temp");

            db.execSQL("DROP TABLE histories_temp");
        }

        //// 14～26 → 27
        if (oldVersion <= 26 && newVersion >= 27) {
            db.execSQL("UPDATE histories SET intent_action_uri = img_uri WHERE source_kind = 'ImgGetterDir' AND intent_action_uri IS NULL");
        }

        //// 14～27 → 28
        if (oldVersion <= 27 && newVersion >= 28) {
            // device_img_uri カラムを作成
            db.execSQL("ALTER TABLE histories ADD COLUMN device_img_uri TEXT;");
        }

        //// 14～28 → 29
        if (oldVersion <= 28 && newVersion >= 29) {
            db.execSQL("DROP TABLE IF EXISTS " + ScreenOffHistoriesModel.TABLE_NAME);
            db.execSQL("CREATE TABLE " + ScreenOffHistoriesModel.TABLE_NAME + " ( " +
                    "`" + ScreenOffHistoriesModel.Columns._ID + "` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    "`" + ScreenOffHistoriesModel.Columns.CREATED_AT + "` TEXT NOT NULL)");
        }
    }

}