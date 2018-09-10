package xyz.monogatari.autowallpaper;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

import xyz.monogatari.autowallpaper.util.MySQLiteOpenHelper;

/**
 * 履歴ページのDBからデータ読み込む用のクラス
 * CursorLoader を使うと生SQL使えないしローダーの機能は今回は合わないと判断したためAsyncTaskでDB読み出しを実装した
 * Created by k-shunsuke on 2018/02/24.
 */
@SuppressWarnings("WeakerAccess")
public class HistoryAsyncTask extends AsyncTask<Void, Void, List<HistoryItemListDataStore>> {
    // --------------------------------------------------------------------
    // フィールド
    // --------------------------------------------------------------------
    private Listener listener;
    private final MySQLiteOpenHelper mDbHelper;

    // --------------------------------------------------------------------
    // コンストラクタ
    // --------------------------------------------------------------------
    public HistoryAsyncTask(MySQLiteOpenHelper dbHelper, Listener listener) {
        super();

        this.mDbHelper = dbHelper;
        this.listener = listener;
    }

    // --------------------------------------------------------------------
    // メソッド、オーバーライド
    // --------------------------------------------------------------------
    /************************************
     * 別スレッドで非同期処理
     */
    @Override
    protected List<HistoryItemListDataStore> doInBackground(Void... voids) {
        SQLiteDatabase db = this.mDbHelper.getReadableDatabase();
        Cursor cursor = null;

        //noinspection TryFinallyCanBeTryWithResources
        try {
            cursor = db.rawQuery("SELECT _id, source_kind, img_uri, intent_action_uri, strftime('%s', created_at) AS created_at_unix FROM histories ORDER BY created_at DESC LIMIT " + HistoryActivity.MAX_RECORD_STORE, null);

            List<HistoryItemListDataStore> itemList = new ArrayList<>();
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    HistoryItemListDataStore item = new HistoryItemListDataStore(
                            cursor.getInt(cursor.getColumnIndexOrThrow("_id")),
                            cursor.getString(cursor.getColumnIndexOrThrow("source_kind")),
                            cursor.getString(cursor.getColumnIndexOrThrow("img_uri")),
                            cursor.getString(cursor.getColumnIndexOrThrow("intent_action_uri")),
                            (long)cursor.getInt(cursor.getColumnIndexOrThrow("created_at_unix"))*1000
                    );
                    itemList.add(item);
                }
            }

            return itemList;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }

    /************************************
     * メインスレッド（UIスレッド）での後処理
     * @param itemList DBから取得した履歴のデータ
     */
    @Override
    protected void onPostExecute(List<HistoryItemListDataStore> itemList) {
        if (this.listener != null) {
            // 履歴ページに表示する
            this.listener.onSuccess(itemList);

            // execute() は new AsyncTask()したら一回しかできないので null 入れてlistenerをnullにする
            this.listener = null;
        }
    }

    // --------------------------------------------------------------------
    // リスナー関係、https://akira-watson.com/android/asynctask.html 参考
    // -------------------------------------------------------------------
// ここの機能はコンストラクタに移しました
//    void setListener(Listener listener) {
//        this.listener = listener;
//    }

    interface Listener {
        void onSuccess(List<HistoryItemListDataStore> itemList);
    }
}
