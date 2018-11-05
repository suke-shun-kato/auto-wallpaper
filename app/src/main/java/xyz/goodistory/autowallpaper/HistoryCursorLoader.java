package xyz.goodistory.autowallpaper;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.AsyncTaskLoader;


@SuppressWarnings("WeakerAccess")
public class HistoryCursorLoader extends AsyncTaskLoader<Cursor> {
    // --------------------------------------------------------------------
    // 
    // --------------------------------------------------------------------
    HistoryCursorLoader(Context context) {
        super(context);
    }

    // --------------------------------------------------------------------
    // 
    // --------------------------------------------------------------------
    /**
     * @return Cursor
     */
    @Override
    public Cursor loadInBackground() {
        HistoryModel historyModel = new HistoryModel(this.getContext());
        return historyModel.getAllHistories();
    }
}


