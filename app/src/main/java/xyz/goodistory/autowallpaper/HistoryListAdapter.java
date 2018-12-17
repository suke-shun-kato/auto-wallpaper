package xyz.goodistory.autowallpaper;


import android.content.Context;
import android.database.Cursor;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * 履歴ページのListViewを作成するためのアダプター
 */

@SuppressWarnings("WeakerAccess")
public class HistoryListAdapter extends CursorAdapter {
    private final LayoutInflater mLayoutInflater;

    /**
     * Recommended constructor.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     * @param flags   Flags used to determine the behavior of the adapter; may
     *                be any combination of {@link #FLAG_AUTO_REQUERY} and
     *                {@link #FLAG_REGISTER_CONTENT_OBSERVER}.
     */
    public HistoryListAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);

        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /**
     * Makes a new view to hold the data pointed to by cursor.
     *
     * @param context Interface to application's global information
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // R.layout.item_list_history はListのItemのレイアウトファイル
        return mLayoutInflater.inflate(R.layout.item_list_history, parent, false);
    }

    /**
     *
     * Bind an existing view to the data pointed to by cursor
     * newView() の後に実行される
     *
     * @param view    Existing view, returned earlier by newView
     * @param context Interface to application's global information
     * @param cursor  The cursor from which to get the data. The cursor is already
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // ----------------------------------
        // 画像
        // ----------------------------------
        //// 画像読み込み処理
        ImageView wpImageView = view.findViewById(R.id.history_item_image);
        String imgUri = cursor.getString(cursor.getColumnIndexOrThrow("img_uri"));

        ImageLoader imgLoader = ImageLoader.getInstance();
        imgLoader.displayImage(imgUri, wpImageView);


        // ----------------------------------
        // 取得元のアイコン（Twitterから、ディレクトリから）
        // ----------------------------------
        ImageView sourceKindImageView = view.findViewById(R.id.history_item_sourceKind);
        String sourceKind = cursor.getString(cursor.getColumnIndexOrThrow("source_kind"));


        Integer rId = HistoryModel.ICON_R_IDS.get(sourceKind);
        if (rId != null) {
            sourceKindImageView.setImageResource(rId);
        }

        // ----------------------------------
        // 更新時間
        // ----------------------------------
        TextView updateTimeTextView = view.findViewById(R.id.history_item_createdAt);
        String yyyymmddhhmmss = cursor.getString(cursor.getColumnIndexOrThrow("created_at"));

        String updateTimeStr;   // 表示する更新時間の文字列
        try {

            long unixTimeMsec = HistoryModel.sqliteToUnixTimeMillis(yyyymmddhhmmss);

            // UTCを入れると「時差」と「言語による表示形式」を考慮した文字列を返してくれる
            updateTimeStr = DateUtils.formatDateTime(
                    context,
                    unixTimeMsec,
                    DateUtils.FORMAT_SHOW_YEAR
                            | DateUtils.FORMAT_SHOW_DATE
                            | DateUtils.FORMAT_SHOW_WEEKDAY
                            | DateUtils.FORMAT_SHOW_TIME
                            | DateUtils.FORMAT_ABBREV_ALL   // ABBREV: 短縮された
            );
        } catch (Exception e) {
            // parse() に失敗したら空文字列を表示する
            updateTimeStr = "";
        }

        // view にセット
        updateTimeTextView.setText(updateTimeStr);
    }



}




