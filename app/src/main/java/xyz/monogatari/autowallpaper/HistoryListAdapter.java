package xyz.monogatari.autowallpaper;


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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import xyz.monogatari.autowallpaper.util.MySQLiteOpenHelper;


/**
 * 履歴ページのListViewを作成するためのアダプター
 */

@SuppressWarnings("WeakerAccess")
public class HistoryListAdapter extends CursorAdapter {
    private LayoutInflater mLayoutInflater;

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


        int rId = MySQLiteOpenHelper.sourceKindToRId(sourceKind);
        sourceKindImageView.setImageResource(rId);

        // ----------------------------------
        // 更新時間
        // ----------------------------------
        TextView updateTimeTextView = view.findViewById(R.id.history_item_createdAt);
        String yyyymmddhhmmss = cursor.getString(cursor.getColumnIndexOrThrow("created_at"));

        SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
        String updateTimeStr;   // 表示する更新時間の文字列
        try {
            Date date = sdFormat.parse(yyyymmddhhmmss);
            long unixTimeMsec = date.getTime();

            // UTCを入れると「時差」と「言語による表示形式」を考慮した文字列を返してくれる
            updateTimeStr = DateUtils.formatDateTime(
                    context,
                    unixTimeMsec,
                    DateUtils.FORMAT_SHOW_YEAR
                            | DateUtils.FORMAT_SHOW_DATE
                            | DateUtils.FORMAT_SHOW_WEEKDAY
                            | DateUtils.FORMAT_SHOW_TIME
                            | DateUtils.FORMAT_ABBREV_ALL
            );
        } catch (Exception e) {
            // parse() に失敗したら空文字列を表示する
            updateTimeStr = "";
        }

        // view にセット
        updateTimeTextView.setText(updateTimeStr);

        // ----------------------------------
        // クリック時のイベント
        // ----------------------------------

    }



}
//
//public class HistoryListAdapter extends SimpleAdapterr {
//    // --------------------------------------------------------------------
//    //
//    // --------------------------------------------------------------------
//    private final Context context;
//    private final int itemRLayout;
//
//    // --------------------------------------------------------------------
//    //
//    // --------------------------------------------------------------------
//    public HistoryListAdapter(Context context, @SuppressWarnings("SameParameterValue") int itemRLayout) {
//        this.context = context;
//        this.itemRLayout = itemRLayout;
//    }
//
//    // --------------------------------------------------------------------
//    //
//    // --------------------------------------------------------------------
//    /************************************
//     * リストのサイズ（itemの数）
//     * @return int リストのitemの数
//     */
//    @Override
//    public int getCount() {
//Log.d("adapter", "nakanaka");
//        return itemList.size();
//    }
//
//    /************************************
//     * @param positionAtList Listの中での位置、インデックス
//     * @return Itemのデータ
//     */
//    @Override
//    public Object getItem(int positionAtList) {
//        return itemList.get(positionAtList);
//    }
//
//    /************************************
//     * @param positionAtList Listの中での位置、インデックス
//     * @return id
//     */
//    @Override
//    public long getItemId(int positionAtList) {
//        return itemList.get(positionAtList).getId();
//    }
//
//    /************************************
//     * @param positionAtList Listの中での位置、インデックス
//     * @param convertItemView 再利用可能なItemのView
//     * @param parentViewGroup 親、ListView
//     * @return 作成したitemのView
//     */
//    @Override
//    public View getView(int positionAtList, View convertItemView, ViewGroup parentViewGroup) {
//
//        // ----------------------------------
//        // convertItemViewの前処理
//        // ----------------------------------
//        Activity activity = (Activity) context;
//
//        if (convertItemView == null) {
//            convertItemView = activity.getLayoutInflater().inflate(this.itemRLayout, null);
//        }
//
//        // ----------------------------------
//        // convertItemViewの各viewごとにレイアウトを作成
//        // ----------------------------------
//        final HistoryItemListDataStore itemDataStore = (HistoryItemListDataStore) this.getItem(positionAtList);
//
//        // ----------
//        // 壁紙画像
//        // ----------
//        //// 画像読み込み処理
//        ImageView wpImageView = convertItemView.findViewById(R.id.history_item_image);
//        String imgUrl = itemDataStore.getImg_uri();
//
//        ImageLoader imgLoader = ImageLoader.getInstance();
//        imgLoader.displayImage(imgUrl, +);
//
//        // ----------
//        // クリックしたらソースに飛ぶように設定
//        // ----------
//        // TODO あとで復活させる
////        wpImageView.setOnClickListener(new View.OnClickListener(){
////            @Override
////            public void onClick(View view) {
////Log.d("○"+this.getClass().getSimpleName(), "imgUri: " + itemDataStore.getImg_uri());
////Log.d("○"+this.getClass().getSimpleName(), "intentUri: " + itemDataStore.getIntent_action_uri());
////                //// intent先のURI
////                String intentUriStr = itemDataStore.getIntent_action_uri();
////                if (intentUriStr == null) {
////                    intentUriStr = itemDataStore.getImg_uri();
////                }
////Log.d("○"+this.getClass().getSimpleName(), "intentUri: " + intentUriStr);
////
////                //// intentをセット
////                Intent intent = new Intent(Intent.ACTION_VIEW);
////                if (intentUriStr.startsWith("content://")) {
////                    intent.setDataAndType(Uri.parse(intentUriStr),"image/*");
////                    intent.addFlags(
////                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION
////                    );
////
////                } else {
////                    intent.setData(Uri.parse(intentUriStr));
////                }
////
////                // resolveActivity() インテントで動作するアクティビティを決める、
////                // 戻り値はコンポーネント（アクティビティとかサービスとか）名オブジェクト
////                if (intent.resolveActivity(context.getPackageManager()) != null) {
////                    context.startActivity(intent);
////                } else {
////Log.d("○"+this.getClass().getSimpleName(), "インテントできません！！！！！");
////                }
////
////            }
////        });
//
//
//        // ----------
//        // 取得元のアイコン画像（Twitterやディレクトリなど）
//        // ----------
//        ImageView iconImageView = convertItemView.findViewById(R.id.history_item_sourceKind);
//        int rId = (int)ImgSourceAsso.get(itemDataStore.getSource_kind()).get("icon");
//        iconImageView.setImageResource(rId);
//
//
//        // ----------
//        // 更新時間
//        // ----------
//        long unixTimeMsec = itemDataStore.getCreated_at_unix(); //表示したい日時（UTC）
//
//        // UTCを入れると「時差」と「言語による表示形式」を考慮した文字列を返してくれる
//        String datetimeStr = DateUtils.formatDateTime(
//                this.context,
//                unixTimeMsec,
//                DateUtils.FORMAT_SHOW_YEAR
//                        | DateUtils.FORMAT_SHOW_DATE
//                        | DateUtils.FORMAT_SHOW_WEEKDAY
//                        | DateUtils.FORMAT_SHOW_TIME
//                        | DateUtils.FORMAT_ABBREV_ALL
//        );
//
//        TextView tv = convertItemView.findViewById(R.id.history_item_createdAt);
//        tv.setText(datetimeStr);
//
//        return convertItemView;
//    }
//}




