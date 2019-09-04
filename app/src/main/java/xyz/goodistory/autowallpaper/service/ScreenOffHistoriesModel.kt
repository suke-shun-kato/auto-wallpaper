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
    }

    object Columns : BaseColumns {
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