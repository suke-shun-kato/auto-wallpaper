package xyz.monogatari.suke.autowallpaper.wpchange;

import android.app.Activity;
import android.content.Context;
import android.support.v4.content.res.ResourcesCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import xyz.monogatari.suke.autowallpaper.R;

/**
 * 履歴ページのListViewを作成するためのアダプター
 * Created by k-shunsuke on 2018/02/07.
 */

public class HistoryListAdapter extends BaseAdapter {
    // --------------------------------------------------------------------
    // 
    // --------------------------------------------------------------------
    private Context context = null;
    private List<HistoryListItem> itemList = null;
    private int itemRLayout = 0;

    // --------------------------------------------------------------------
    // 
    // --------------------------------------------------------------------
    public HistoryListAdapter(Context context, List<HistoryListItem> itemList, int itemRLayout) {
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
        // convertItemViewの書くviewごとにレイアウトを作成
        // ----------------------------------
        HistoryListItem item = (HistoryListItem) this.getItem(positionAtList);

        // ----------
        // 壁紙画像
        // ----------
        ImageView iv1 = (ImageView)convertItemView.findViewById(R.id.history_item_image);
        iv1.setImageDrawable(
                 ResourcesCompat.getDrawable(this.context.getResources(), R.drawable.btn_power_off_24dp, null)
        );

        // todo 今仮画像入れてるだけ
        //// 画像取得
        //// クリック時に取得元に飛ぶように修正

        // ----------
        // 取得元のアイコン画像（Twitterやディレクトリなど）
        // ----------
        // todo アイコンにちゃんと修正する,ImageViewにする
        TextView iv2 = (TextView)convertItemView.findViewById(R.id.history_item_sourceKind);
        iv2.setText( "" + item.getSource_kind() );

        // ----------
        // 更新時間
        // ----------
        // todo 時刻表示をその地域に合わせてちゃんとする
        TextView tv = (TextView)convertItemView.findViewById(R.id.history_item_createdAt);
        tv.setText( item.getCreated_at_local() );


        return convertItemView;
    }
}
