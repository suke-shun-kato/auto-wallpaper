package xyz.goodistory.autowallpaper;

import android.content.Context;
import android.database.Cursor;
import androidx.loader.content.AsyncTaskLoader;

import xyz.goodistory.autowallpaper.util.MySQLiteOpenHelper;


@SuppressWarnings("WeakerAccess")
public class HistoryCursorLoader extends AsyncTaskLoader<Cursor> {
    private final MySQLiteOpenHelper mDbHelper;

    // --------------------------------------------------------------------
    //
    // --------------------------------------------------------------------
    HistoryCursorLoader(Context context, MySQLiteOpenHelper dbHelper) {
        super(context);

        mDbHelper = dbHelper;
    }

    // --------------------------------------------------------------------
    // 
    // --------------------------------------------------------------------
    /**
     * @return Cursor
     */
    @Override
    public Cursor loadInBackground() {
        HistoryModel historyModel = new HistoryModel(getContext(), mDbHelper);
        return historyModel.getAllHistories();
    }
}


