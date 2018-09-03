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
     * TODO クエリの部分をなんとかする
     * @return
     */
    @Override
    public Cursor loadInBackground() {


        MySQLiteOpenHelper dbHelper = MySQLiteOpenHelper.getInstance(this.getContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT id, source_kind, img_uri, intent_action_uri, strftime('%s', created_at) AS created_at_unix FROM histories ORDER BY created_at DESC LIMIT " + HistoryActivity.MAX_RECORD_STORE, null);

        return cursor;


    }
}


