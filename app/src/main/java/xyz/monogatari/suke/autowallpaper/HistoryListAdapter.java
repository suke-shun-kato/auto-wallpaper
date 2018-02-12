package xyz.monogatari.suke.autowallpaper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;
import java.util.TimeZone;


/**
 * 履歴ページのListViewを作成するためのアダプター
 * Created by k-shunsuke on 2018/02/07.
 */

@SuppressWarnings("WeakerAccess")
public class HistoryListAdapter extends BaseAdapter {
    // --------------------------------------------------------------------
    // 
    // --------------------------------------------------------------------
    private Context context = null;
    private List<HistoryItemListDataStore> itemList = null;
    private int itemRLayout = 0;

    // --------------------------------------------------------------------
    // 
    // --------------------------------------------------------------------
    public HistoryListAdapter(Context context, List<HistoryItemListDataStore> itemList, int itemRLayout) {
        this.context = context;
        this.itemList = itemList;
        this.itemRLayout = itemRLayout;
    }

    // --------------------------------------------------------------------
    //
    // --------------------------------------------------------------------
    /************************************
     * リストのサイズ（itemの数）
     * @return int リストのitemの数
     */
    @Override
    public int getCount() {
        return itemList.size();
    }

    /************************************
     * @param positionAtList Listの中での位置、インデックス
     * @return Itemのデータ
     */
    @Override
    public Object getItem(int positionAtList) {
        return itemList.get(positionAtList);
    }

    /************************************
     * @param positionAtList Listの中での位置、インデックス
     * @return id
     */
    @Override
    public long getItemId(int positionAtList) {
        return itemList.get(positionAtList).getId();
    }

    /************************************
     * @param positionAtList Listの中での位置、インデックス
     * @param convertItemView 再利用可能なItemのView
     * @param parentViewGroup 親、ListView
     * @return 作成したitemのView
     */
    @Override
    public View getView(int positionAtList, View convertItemView, ViewGroup parentViewGroup) {

        // ----------------------------------
        // convertItemViewの前処理
        // ----------------------------------
        Activity activity = (Activity) context;

        if (convertItemView == null) {
            convertItemView = activity.getLayoutInflater().inflate(this.itemRLayout, null);
        }

        // ----------------------------------
        // convertItemViewの各viewごとにレイアウトを作成
        // ----------------------------------
        final HistoryItemListDataStore itemDataStore = (HistoryItemListDataStore) this.getItem(positionAtList);

        // ----------
        // 壁紙画像
        // ----------
        //// 画像読み込み処理
        ImageView wpImageView = convertItemView.findViewById(R.id.history_item_image);
        String imgUrl = itemDataStore.getImg_uri();

        ImageLoader imgLoader = ImageLoader.getInstance();
        imgLoader.displayImage(imgUrl, wpImageView);

        // ----------
        // クリックしたらソースに飛ぶように設定
        // ----------
        wpImageView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
Log.d("○"+this.getClass().getSimpleName(), "imgUri: " + itemDataStore.getImg_uri());
Log.d("○"+this.getClass().getSimpleName(), "intentUri: " + itemDataStore.getIntent_action_uri());
                //// intent先のURI
                String intentUriStr = itemDataStore.getIntent_action_uri();
                if (intentUriStr == null) {
                    intentUriStr = itemDataStore.getImg_uri();
                }
Log.d("○"+this.getClass().getSimpleName(), "intentUri: " + intentUriStr);

                //// intentをセット
                Intent intent = new Intent(Intent.ACTION_VIEW);
                if (intentUriStr.startsWith("content://")) {
                    intent.setDataAndType(Uri.parse(intentUriStr),"image/*");
                    intent.addFlags(
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION
                    );

                } else {
                    intent.setData(Uri.parse(intentUriStr));
                }

                // resolveActivity() インテントで動作するアクティビティを決める、
                // 戻り値はコンポーネント（アクティビティとかサービスとか）名オブジェクト
                if (intent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(intent);
                } else {
Log.d("○"+this.getClass().getSimpleName(), "インテントできません！！！！！");
                }

            }
        });

        // ----------
        // 取得元のアイコン画像（Twitterやディレクトリなど）
        // ----------
        ImageView iv2 = (ImageView)convertItemView.findViewById(R.id.history_item_sourceKind);
        int rId = (int)ImgSourceAsso.get(itemDataStore.getSource_kind()).get("icon");
        iv2.setImageResource(rId);


        // ----------
        // 更新時間
        // ----------
        long unixTimeMsec = itemDataStore.getCreated_at_unix(); //表示したい日時（UTC）
        // getRawOffset(): 時差、getDSTSavings():サマータイムなどの追加時間
        long timeOffsetMsec = TimeZone.getDefault().getRawOffset() + TimeZone.getDefault().getDSTSavings(); //時差

        String datetimeStr = DateUtils.formatDateTime(
                this.context,
                unixTimeMsec + timeOffsetMsec,
                DateUtils.FORMAT_SHOW_YEAR
                        | DateUtils.FORMAT_SHOW_DATE
                        | DateUtils.FORMAT_SHOW_WEEKDAY
                        | DateUtils.FORMAT_SHOW_TIME
                        | DateUtils.FORMAT_ABBREV_ALL
        );

        TextView tv = (TextView)convertItemView.findViewById(R.id.history_item_createdAt);
        tv.setText(datetimeStr);

        return convertItemView;
    }
}




