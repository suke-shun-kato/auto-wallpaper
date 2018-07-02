package xyz.monogatari.autowallpaper;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.List;

import xyz.monogatari.autowallpaper.util.MySQLiteOpenHelper;

/**
 * 履歴ページ、ひとまず作成
 * Created by k-shunsuke on 2017/12/20.
 */

public class HistoryActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {




    // --------------------------------------------------------------------
    // フィールド
    // --------------------------------------------------------------------
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView _lv;

    // --------------------------------------------------------------------
    // 定数
    // --------------------------------------------------------------------
//    /** 履歴ページに表示する件数 */
//    public static int MAX_RECORD_DISPLAY = 100;   //DBに保存する履歴件数の方で調整できるので不要
    /** DBに保存する履歴件数 */
    public static final int MAX_RECORD_STORE = 100;

    // --------------------------------------------------------------------
    // メソッド（オーバーライド）
    // --------------------------------------------------------------------
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_history);

        _lv = findViewById(R.id.history_list);

        // ----------------------------------
        // アクションバーの設定
        // ----------------------------------
        ////　ツールバーをアクションバーとして表示
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        this.setSupportActionBar(myToolbar);

        //// アクションバーに「←」ボタンを表示、
        // onOptionsItemSelected(){} でボタンを押したときのリスナを設定する必要はない
        // （XMLで親アクティビティを設定しているので）
        // ここのActionBar は android.support.v7.app.ActionBar の方のクラスになる
        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // ----------------------------------
        // 画像ローダーの初期設定
        // ----------------------------------
        //// displayImage() 関数の設定
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
            // ダウンロード中の表示画像
            .showImageOnLoading(R.drawable.anim_refresh)
            // URLが空だったときの表示画像
            .showImageForEmptyUri(R.drawable.ic_history_remove)
            // ネット未接続やURLが間違っていて失敗したときの表示画像
            .showImageOnFail(R.drawable.ic_history_error)
            // メモリにキャッシュを有効
            .cacheInMemory(true)
//           .cacheOnDisk(true)
            .build();

        //// imageLoader自体の設定
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this.getApplicationContext())
            .defaultDisplayImageOptions(defaultOptions)
            .memoryCacheSizePercentage(25)
            .build();
        ImageLoader.getInstance().init(config);

        // ----------------------------------
        // DBから取得したデータを表示
        // ----------------------------------
        this.updateListView();

        // ----------------------------------
        // リフレッシュのグルグルの設定
        // ----------------------------------
        this.swipeRefreshLayout = findViewById(R.id.history_swipe_refresh);
        this.swipeRefreshLayout.setOnRefreshListener(this);
        this.swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);

        // ----------------------------------
        // 長押し時のコンテキストメニューの表示
        // ----------------------------------
        this.registerForContextMenu(_lv);
    }


    /**
     * Called when a context menu for the {@code view} is about to be shown.
     * Unlike {@link #onCreateOptionsMenu(Menu)}, this will be called every
     * time the context menu is about to be shown and should be populated for
     * the view (or item inside the view for {@link AdapterView} subclasses,
     * this can be found in the {@code menuInfo})).
     * <p>
     * Use {@link #onContextItemSelected(MenuItem)} to know when an
     * item has been selected.
     * <p>
     * It is not safe to hold onto the context menu after this method returns.
     *
     * 長押ししたときに出るコンテキストメニューが作成されたとき
     *
     *
     * @param menu
     * @param v
     * @param menuInfo
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
Log.d("aaaaaaaaaaaaaaaaaaaaa", "ccccccccccccccccccc");

        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater inflater = this.getMenuInflater();
        inflater.inflate(R.menu.menu_history, menu);

        menu.setHeaderTitle(R.string.histories_contextMenu_title);
    }

    /*************************************
     * Handle onNewIntent() to inform the fragment manager that the
     * state is not saved.  If you are handling new intents and may be
     * making changes to the fragment state, you want to be sure to call
     * through to the super-class here first.  Otherwise, if your state
     * is saved but the activity is not stopped, you could get an
     * onNewIntent() call which happens before onResume() and trying to
     * perform fragment operations at that point will throw IllegalStateException
     * because the fragment manager thinks the state is still saved.
     *
     * 通知から起動したとき呼ばれる
     * @param intent このアクティビティを起動したときに送ったインテント
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        this.updateListView();
    }

    /*************************************
     * Called when a swipe gesture triggers a refresh.
     * 下スワイプしたときに呼ばれる
     */
    @Override
    public void onRefresh() {
        // リストの更新
        this.updateListView();
    }

    // --------------------------------------------------------------------
    // メソッド、通常
    // --------------------------------------------------------------------
    /************************************
     * 履歴のListViewを更新する
     */
    private void updateListView() {
        HistoryAsyncTask hat = new HistoryAsyncTask(
                new MySQLiteOpenHelper(this), this.createListener()
        );
        hat.execute();
    }

    private HistoryAsyncTask.Listener createListener() {
        return new HistoryAsyncTask.Listener() {
            @Override
            public void onSuccess(List<HistoryItemListDataStore> itemList) {
                //// 履歴を表示する
//                ListView lv = findViewById(R.id.history_list);
                HistoryListAdapter adapter = new HistoryListAdapter(
                        HistoryActivity.this, itemList, R.layout.item_list_history
                );
                _lv.setAdapter(adapter);

                //// グルグルあればを消す
                swipeRefreshLayout.setRefreshing(false);
            }
        };
    }
}
