package xyz.goodistory.autowallpaper.service

import android.database.DatabaseUtils
import android.provider.BaseColumns
import xyz.goodistory.autowallpaper.util.MySQLiteOpenHelper

/**
 * 下記を参考に実装
 * https://developer.android.com/training/data-storage/sqlite
 * ※Room Persistence Library は使わない
 */
class ScreenOffHistoriesModel(private val dbHelper: MySQLiteOpenHelper) {
    companion object {
        const val TABLE_NAME: String = "screen_off_histories"

        const val SQL_CREATE_ENTRIES: String = """
CREATE TABLE $TABLE_NAME (
    `${Columns.ID}` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    `${Columns.CREATED_AT}` TEXT NOT NULL
)            
"""
        const val SQL_DELETE_ENTRIES  = "DROP TABLE IF EXISTS $TABLE_NAME"
    }

    object Columns {
        const val ID: String = BaseColumns._ID
        const val CREATED_AT: String = "created_at"
    }

    fun getCount(): Long {
        return DatabaseUtils.queryNumEntries(dbHelper.readableDatabase, TABLE_NAME)
    }

    fun countUp() {
        // dbWritable.insert() だと datetime('now') ができない
        dbHelper.writableDatabase.execSQL(
                "INSERT INTO $TABLE_NAME (${Columns.CREATED_AT}) " +
                        "VALUES (datetime('now'))")
    }

    /**
     * 全て削除
     */
    fun reset() {
        dbHelper.writableDatabase.delete(TABLE_NAME, null, null)
    }
}