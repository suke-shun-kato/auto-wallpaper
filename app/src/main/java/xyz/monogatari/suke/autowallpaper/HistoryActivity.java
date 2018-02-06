package xyz.monogatari.suke.autowallpaper;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import xyz.monogatari.suke.autowallpaper.util.MySQLiteOpenHelper;

/**
 * 履歴ページ、ひとまず作成
 * Created by k-shunsuke on 2017/12/20.
 */

public class HistoryActivity extends AppCompatActivity {
    // --------------------------------------------------------------------
    //
    // --------------------------------------------------------------------
    private MySQLiteOpenHelper mDbHelper;

    // --------------------------------------------------------------------
    // メソッド（オーバーライド）
    // --------------------------------------------------------------------
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_history);

        this.mDbHelper = new MySQLiteOpenHelper(this);

        this.aaaaaa();
    }

    // --------------------------------------------------------------------
    // メソッド
    // --------------------------------------------------------------------
    private void aaaaaa() {
        SQLiteDatabase db = this.mDbHelper.getReadableDatabase();
        Cursor cursor = null;

        //noinspection TryFinallyCanBeTryWithResources
        try {
            cursor = db.rawQuery("SELECT *, datetime(created_at, 'localtime') AS created_at_local  FROM histories ORDER BY created_at DESC", null);

            int id, sourceKind;
            String imgStr, actionStr, createdAtLocal;

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    sourceKind = cursor.getInt(cursor.getColumnIndexOrThrow("source_kind"));
                    imgStr = cursor.getString(cursor.getColumnIndexOrThrow("img_uri"));
                    actionStr = cursor.getString(cursor.getColumnIndexOrThrow("intent_action_uri"));
                    createdAtLocal = cursor.getString(cursor.getColumnIndexOrThrow("created_at_local"));



Log.d("○○○○○○" + this.getClass().getSimpleName(), "id:" + id + ", img:" + imgStr + ", action: " + actionStr + ", createdAt:" + createdAtLocal);
                }
            }

        } finally {
            cursor.close();
            db.close();
        }

    }
}
