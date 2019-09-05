package xyz.goodistory.autowallpaper.util

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import xyz.goodistory.autowallpaper.HistoryModel

import xyz.goodistory.autowallpaper.service.ScreenOffHistoriesModel

/**
 * Created by k-shunsuke on 2018/02/04.
 * データベースヘルパークラス
 */

class MySQLiteOpenHelper(context: Context)
    : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        // --------------------------------------------------------------------
        //
        // --------------------------------------------------------------------
        // If you change the database schema, you must increment the database version.
        private const val DATABASE_VERSION = 29

        /**
         * デバッグ用のデータベースのバージョン、onUpgrade()のoldVersionの値になる
         * 0にするとonCreate() が呼ばれる
         */
        private val DATABASE_OLD_VERSION_FOR_DEBUG: Int? = null

        private const val DATABASE_NAME = "master.sqlite3"

        private var sMySQLiteOpenHelper: MySQLiteOpenHelper? = null

        @Synchronized
        @JvmStatic
        fun getInstance(context: Context): MySQLiteOpenHelper {
            if (sMySQLiteOpenHelper == null) {
                sMySQLiteOpenHelper = MySQLiteOpenHelper(context)
            }
            return sMySQLiteOpenHelper!!
        }
    }
    // --------------------------------------------------------------------
    // オーバーライド
    // --------------------------------------------------------------------
    /**
     * @param db The database.
     */
    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)

        //// デバッグ用にバージョンをセットする、onUpgrade()のoldVersionの値になる
        if (DATABASE_OLD_VERSION_FOR_DEBUG != null) {
            db.version = DATABASE_OLD_VERSION_FOR_DEBUG
        }
    }

    /**
     * 初回に呼ばれるメソッド
     * 初回は onUpgrade() は呼ばれないので、ここで全てのテーブルを作成する必要がある
     */
    override fun onCreate(db: SQLiteDatabase) {
        //// histories 作成
        db.execSQL(HistoryModel.SQL_DELETE_ENTRIES)
        db.execSQL(HistoryModel.SQL_CREATE_ENTRIES)
        db.execSQL(HistoryModel.SQL_CREATE_INDEX)

        //// screenOffHistories
        db.execSQL(ScreenOffHistoriesModel.SQL_DELETE_ENTRIES)
        db.execSQL(ScreenOffHistoriesModel.SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        //// 1～25 → 26
        if (oldVersion <= 25 && newVersion >= 26) {
            //// id → _id カラムに変更
            db.execSQL("DROP TABLE IF EXISTS histories_temp")
            db.execSQL("ALTER TABLE histories RENAME TO histories_temp")

            db.execSQL("CREATE TABLE histories (" + "`_id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " + "`source_kind` TEXT NOT NULL, " + "`img_uri` TEXT NOT NULL, " + "`intent_action_uri` TEXT, " + "`created_at` TEXT NOT NULL )")
            // 同じ名前のインデックスを作成したらエラーがでるので、
            // その前に変更前のテーブルのインデックスを削除
            db.execSQL("DROP INDEX IF EXISTS created_at")
            db.execSQL("CREATE INDEX created_at ON histories(created_at)")

            db.execSQL("INSERT INTO histories(source_kind, img_uri, intent_action_uri, created_at) " + "SELECT source_kind, img_uri, intent_action_uri, created_at FROM histories_temp")

            db.execSQL("DROP TABLE histories_temp")
        }

        //// 1～26 → 27
        if (oldVersion <= 26 && newVersion >= 27) {
            val sql = """
UPDATE ${HistoryModel.TABLE_NAME} 
SET ${HistoryModel.Columns.INTENT_ACTION_URI} = ${HistoryModel.Columns.IMG_URI} 
WHERE ${HistoryModel.Columns.SOURCE_KIND} = '${HistoryModel.SOURCE_DIR}'
    AND ${HistoryModel.Columns.INTENT_ACTION_URI} IS NULL
"""
            db.execSQL(sql)
        }

        //// 1～27 → 28
        if (oldVersion <= 27 && newVersion >= 28) {

            // device_img_uri カラムを作成
            val sql = """
ALTER TABLE ${HistoryModel.TABLE_NAME} ADD COLUMN ${HistoryModel.Columns.DEVICE_IMG_URI} TEXT;     
"""
            db.execSQL(sql)
        }

        //// 1～28 → 29
        if (oldVersion <= 28 && newVersion >= 29) {
            db.execSQL(ScreenOffHistoriesModel.SQL_DELETE_ENTRIES)
            db.execSQL(ScreenOffHistoriesModel.SQL_CREATE_ENTRIES)
        }
    }



}