package xyz.goodistory.autowallpaper.service

import android.content.Context
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase
import xyz.goodistory.autowallpaper.util.MySQLiteOpenHelper

/**
 * TODO 下記を使ったほうがよいかもしれない
 * https://developer.android.com/training/data-storage/room#kotlin
 */
class ScreenOffHistoriesModel(context: Context) {
    private val dbReadable: SQLiteDatabase
    val dbWritable: SQLiteDatabase

    init {
        val dbHelper: MySQLiteOpenHelper = MySQLiteOpenHelper.getInstance(context)
        dbReadable = dbHelper.readableDatabase
        dbWritable = dbHelper.writableDatabase
    }


    companion object {
        const val TABLE_NAME: String = "screen_off_histories"
        val PROJECTION: Array<String> = arrayOf(
                "_id",
                "created_at"
        )
    }

    fun getCount(): Long {
        return DatabaseUtils.queryNumEntries(dbReadable, TABLE_NAME)
    }

    fun countUp() {
        // dbWritable.insert() だと datetime('now') ができない
        dbWritable.execSQL(
                "INSERT INTO $TABLE_NAME (created_at) VALUES (datetime('now'))")
    }

    fun reset() {
        dbWritable.delete(TABLE_NAME, null, null)
    }

    fun dbClose() {
        dbReadable.close()
        dbWritable.close()
    }

}