package xyz.monogatari.autowallpaper;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.AsyncTaskLoader;

import xyz.monogatari.autowallpaper.util.MySQLiteOpenHelper;

public class HistoryCursorLoader extends AsyncTaskLoader<Cursor> {

    HistoryCursorLoader(Context context) {
        super(context);
    }

    /**
     * @return Cursor
     */
    @Override
    public Cursor loadInBackground() {
        MySQLiteOpenHelper dbHelper = MySQLiteOpenHelper.getInstance(this.getContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        return db.query(
                MySQLiteOpenHelper.TABLE_HISTORIES,
                MySQLiteOpenHelper.HISTORIES_PROJECTION,
                null, null, null, null,
                "created_at DESC",
                String.valueOf(HistoryActivity.MAX_RECORD_STORE));
    }
}


