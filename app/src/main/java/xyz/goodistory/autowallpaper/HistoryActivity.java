package xyz.goodistory.autowallpaper;

import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import xyz.goodistory.autowallpaper.util.DisplaySizeCheck;
import xyz.goodistory.autowallpaper.util.ProgressBcastReceiver;
import xyz.goodistory.autowallpaper.wpchange.WpManagerService;

/**
 * 履歴ページ
 * Created by k-shunsuke on 2017/12/20.
 */
public class HistoryActivity
        extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>,
        SwipeRefreshLayout.OnRefreshListener,
        ProgressBcastReceiver.OnStateChangeListener {

    // --------------------------------------------------------------------
    // フィールド
    // --------------------------------------------------------------------
//    private AdView mAdView;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private HistoryListAdapter mAdapter = null;
    private LoaderManager mLoaderManager;

    // 壁紙変更状態を検知するブロードキャストレシーバー
    private ProgressBcastReceiver mProgressBcastReceiver;

    private int mLoaderId;

    private AdView mAdView = null;

    // --------------------------------------------------------------------
    // 定数
    // --------------------------------------------------------------------
    /** DBに保存する履歴件数 */
    public static final int MAX_RECORD_STORE = 100;
//    public static final int MAX_RECORD_STORE = 10; // TODO 元に戻す

    // --------------------------------------------------------------------
    // --------------------------------------------------------------------
    // メソッド（オーバーライド）
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_history);

        // ----------------------------------
        // 広告の設定
        // ----------------------------------
        //// 初期化
        MobileAds.initialize(this, getString(R.string.id_adMob_appId));


        //// 必要な変数の準備
        LinearLayout addContainerLayout = findViewById(R.id.history_add_container);

        int displayWidth = DisplaySizeCheck.getScreenWidthInDPs(this);//ディスプレイの横幅取得
        int pdLeftPx = addContainerLayout.getPaddingLeft();
        int pdRightPx = addContainerLayout.getPaddingRight();
        float scale = this.getResources().getDisplayMetrics().density; // dp * scale = pixel
        float paddingSumDp =  (pdLeftPx + pdRightPx) / scale; // padding の dp を計算

        // 広告サイズより小さいデバイスは広告を表示させない
        if (displayWidth >= AdSize.BANNER.getWidth() + paddingSumDp) {
            ////
            mAdView = new AdView(this);

            //// バナーのサイズを指定
            // SMART_BANNER は自動でサイズを決定してくれるが、端に変な表示が入るので使わない
            if ( displayWidth < AdSize.FULL_BANNER.getWidth() + paddingSumDp) {
                mAdView.setAdSize(AdSize.BANNER); // 320x50
            } else if( displayWidth >= AdSize.FULL_BANNER.getWidth() + paddingSumDp
                    && displayWidth < AdSize.LEADERBOARD.getWidth() + paddingSumDp) {
                mAdView.setAdSize(AdSize.FULL_BANNER);  // 468x60
            } else if (displayWidth >= AdSize.LEADERBOARD.getWidth() + paddingSumDp) {
                mAdView.setAdSize(AdSize.LEADERBOARD);  // 728x90
            }

            //// バナーIDをセット
            mAdView.setAdUnitId( getString(R.string.id_adMob_addUnitId) );

            //// バナーをViewにセット
            addContainerLayout.addView(mAdView, 0);
        }

        // ----------------------------------
        // アクションバーの設定
        // ----------------------------------
        //// ツールバーをアクションバーとして表示
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
        // 履歴リストの読み込み設定
        // ----------------------------------
        //// 変数セット
        mLoaderId = getResources().getInteger(R.integer.id_loader_histories);

        //// アダプターをセット（まだビューには反映していない）
        ListView listView = findViewById(R.id.history_list);
        mAdapter = new HistoryListAdapter(
                this, null, HistoryListAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        listView.setAdapter(mAdapter);

        //// ローダーの初期化、読み込みが始まる
        mLoaderManager = getSupportLoaderManager();
        // ここはappcompatを使っているときはforceLoad()をしないとだめ
        // （参考）https://stackoverflow.com/questions/10524667/android-asynctaskloader-doesnt-start-loadinbackground
        mLoaderManager.initLoader(mLoaderId, null, this).forceLoad();


        // ----------------------------------
        // リフレッシュのグルグルの設定
        // ----------------------------------
        mSwipeRefreshLayout = findViewById(R.id.history_swipe_refresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);

        // ----------------------------------
        // 長押し時のコンテキストメニューの表示
        // ----------------------------------
        registerForContextMenu(listView);
        listView.setOnItemClickListener(new HistoryActivity.ListItemClickListener());

        // ----------------------------------
        // IntentServiceでの壁紙変更状態のブロードキャストを受信するレシーバーの設置
        // ----------------------------------
        mProgressBcastReceiver = new ProgressBcastReceiver();
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(WpManagerService.ACTION_WPCHANGE_STATE);
        this.registerReceiver(mProgressBcastReceiver, iFilter);
    }

    /**
     * Dispatch onResume() to fragments.  Note that for better inter-operation
     * with older versions of the platform, at the point of this call the
     * fragments attached to the activity are <em>not</em> resumed.  This means
     * that in some cases the previous state may still be saved, not allowing
     * fragment transactions that modify the state.  To correctly interact
     * with fragments in their proper state, you should instead override
     * {@link #onResumeFragments()}.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.loadAd(new AdRequest.Builder().build());
        }
    }

    @Override
    protected void onDestroy() {
        //// DBから履歴を読み込むローダーをnullにする
        mLoaderManager = null;

        //// 壁紙セット状態を検知するBroadcastReceiverを解除
        unregisterReceiver(mProgressBcastReceiver);

        super.onDestroy();
    }


    // --------------------------------------------------------------------
    //
    // --------------------------------------------------------------------

    /************************************
     * listViewをクリックしたときのリスナー
     */
    private class ListItemClickListener implements AdapterView.OnItemClickListener {
        /**
         * Callback method to be invoked when an item in this AdapterView has
         * been clicked.
         * <p>
         * Implementers can call getItemAtPosition(position) if they need
         * to access the data associated with the selected item.
         *
         * @param parent   The AdapterView where the click happened.
         * @param view     The view within the AdapterView that was clicked (this
         *                 will be a view provided by the adapter)
         * @param position The position of the view in the adapter.
         * @param id       The row id of the item that was clicked. データベースの_idカラムの値
         */
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (canJumpToSource(id)) {
                boolean hasJumped = jumpToSource(id);
                if (!hasJumped) {
                    Toast.makeText(HistoryActivity.this, R.string.histories_cant_jump, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(HistoryActivity.this, R.string.histories_cant_jump, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 指定のhistory_idの履歴がソースにジャンプ可能化どうが
     * @param history_id histories.id
     * @return jump可能かどうか
     */
    private boolean canJumpToSource(long history_id) {
        HistoryModel history_mdl = new HistoryModel(this);
        Cursor cursor = history_mdl.getHistoryById(history_id);

        boolean hasGotCursor = cursor.moveToFirst();
        if (!hasGotCursor) {
            throw new IllegalStateException("history_id: " + history_id + " のレコードは存在しません");
        }

        String intentActionUri = cursor.getString(
                cursor.getColumnIndexOrThrow("intent_action_uri"));

        return intentActionUri != null;
    }
    /**
     * 壁紙の取得元にジャンプ（Intent）する関数
     * @param id historiesテーブルの_idの値
     * @return 取得元にジャンプできたかどうが
     */
    @SuppressWarnings("UnusedReturnValue")
    private boolean jumpToSource(long id) {
        // ----------------------------------
        // DBから取得 ジャンプ先のURIを取得
        // ----------------------------------
        String intentUriStr;
        try {
            HistoryModel historyModel = new HistoryModel(this);
            Cursor cursor = historyModel.getHistoryById(id);
            boolean canGetCursor = cursor.moveToFirst();
            if (!canGetCursor) {
                return false;
            }

            //// intent先のURI
            intentUriStr = cursor.getString(
                    cursor.getColumnIndexOrThrow("intent_action_uri"));
            cursor.close();

            // ----------------------------------
            // intent_action_uri がnullのとき
            // ----------------------------------
            if (intentUriStr == null) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }

        // ----------------------------------
        // ジャンプ
        // ----------------------------------
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

        // resolveActivity() インテントで動作するアクティビティを取得、
        // 戻り値はコンポーネント（アクティビティとかサービスとか）名オブジェクト
        // https://developer.android.com/guide/components/intents-common?hl=ja
        if (intent.resolveActivity(this.getPackageManager()) != null) {
            try {
                this.startActivity(intent);
            } catch (Exception e) {
                return false;
            }
        } else {
            return false;
        }

        return true;
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
    @SuppressWarnings({"JavadocReference", "JavaDoc"})
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        // ----------------------------------
        // メニューをセット（タイトル除く）
        // ----------------------------------
        //// inflate
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_history, menu);

        //// intent_action_uri がnullのときはjumpボタンは色を変える
        // クエリ実行して Cursor取得
        long history_id = ((AdapterView.AdapterContextMenuInfo)menuInfo).id;
        if (!canJumpToSource(history_id)) {
            MenuItem menuItem = menu.findItem(R.id.histories_contextMenu_item_jump);
            if (menuItem != null) {
                menuItem.setEnabled(false);
            }
        }

        // ----------------------------------
        // タイトルをセット
        // ----------------------------------
        menu.setHeaderTitle(R.string.histories_contextMenu_title);
    }

    /**
     * This hook is called whenever an item in a context menu is selected. The
     * default implementation simply returns false to have the normal processing
     * happen (calling the item's Runnable or sending a message to its Handler
     * as appropriate). You can use this method for any items for which you
     * would like to do processing without those other facilities.
     * <p>
     * Use {@link MenuItem#getMenuInfo()} to get extra information set by the
     * View that added this menu item.
     * <p>
     * Derived classes should call through to the base class for it to perform
     * the default menu handling.
     *
     * @param item The context menu item that was selected.
     * @return boolean Return false to allow normal context menu processing to
     * proceed, true to consume it here.
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        int menuItemId = item.getItemId();
        switch (menuItemId) { // コンテキストメニューの選択した項目によって処理を分ける
            case R.id.histories_contextMenu_item_jump:
                boolean hasJumped = jumpToSource(info.id);
                if (!hasJumped) {
                    Toast.makeText(this, R.string.histories_cant_jump, Toast.LENGTH_SHORT).show();
                }

                break;

            case R.id.histories_contextMenu_item_wpSet:
                HistoryModel historyModel = new HistoryModel(this);
                Cursor cursor = historyModel.getHistoryById(info.id);
                boolean canGetCursor = cursor.moveToFirst();
                if (!canGetCursor) {
                    return false;
                }

                String sourceKind = cursor.getString(
                        cursor.getColumnIndexOrThrow("source_kind"));
                String imgUri = cursor.getString(
                        cursor.getColumnIndexOrThrow("img_uri"));
                String intentActionUri = cursor.getString(
                        cursor.getColumnIndexOrThrow("intent_action_uri"));


                WpManagerService.changeWpSpecified(this, imgUri, sourceKind, intentActionUri);
                break;

            case R.id.histories_contextMenu_item_delete:
                try {
                    HistoryModel historyModel2 = new HistoryModel(this);
                    int numDeletedRows = historyModel2.deleteHistories(info.id);

                    if (numDeletedRows >= 1) {
                        // historiesテーブルから再読込して表示
                        mLoaderManager.initLoader(mLoaderId, null, this).forceLoad();
                    } else {
                        throw new RuntimeException("履歴削除エラー");
                    }
                } catch (Exception e) {
                    Toast.makeText(this, R.string.histories_contextMenu_item_delete_errorMessage, Toast.LENGTH_SHORT).show();
                }
                break;
        }


        return super.onContextItemSelected(item);
    }

    /*************************************
     * Called when a swipe gesture triggers a refresh.
     * 下スワイプしたときに呼ばれる
     */
    @Override
    public void onRefresh() {
        //// 履歴データを読み込み始める
        mLoaderManager.initLoader(mLoaderId, null, this).forceLoad();

        //// グルグルあればを消す
        mSwipeRefreshLayout.setRefreshing(false);
    }

    // --------------------------------------------------------------------
    // HistoryCursorLoaderのコールバック
    // --------------------------------------------------------------------
    /**
     * Instantiate and return a new Loader for the given ID.
     *
     * @param id   The ID whose loader is to be created.
     * @param args Any arguments supplied by the caller.
     * @return Return a new Loader instance that is ready to start loading.
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new HistoryCursorLoader(this);
    }

    /**
     * Called when a previously created loader has finished its load.  Note
     * that normally an application is <em>not</em> allowed to commit fragment
     * transactions while in this call, since it can happen after an
     * activity's state is saved.  See {@link FragmentManager#beginTransaction()
     * FragmentManager.openTransaction()} for further discussion on this.
     * <p>
     * <p>This function is guaranteed to be called prior to the release of
     * the last data that was supplied for this Loader.  At this point
     * you should remove all use of the old data (since it will be released
     * soon), but should not do your own release of the data since its Loader
     * owns it and will take care of that.  The Loader will take care of
     * management of its data so you don't have to.  In particular:
     * <p>
     * <ul>
     * <li> <p>The Loader will monitor for changes to the data, and report
     * them to you through new calls here.  You should not monitor the
     * data yourself.  For example, if the data is a {@link Cursor}
     * and you place it in a {@link CursorAdapter}, use
     * the {@link CursorAdapter#CursorAdapter(Context, * Cursor, int)} constructor <em>without</em> passing
     * in either {@link CursorAdapter#FLAG_AUTO_REQUERY}
     * or {@link CursorAdapter#FLAG_REGISTER_CONTENT_OBSERVER}
     * (that is, use 0 for the flags argument).  This prevents the CursorAdapter
     * from doing its own observing of the Cursor, which is not needed since
     * when a change happens you will get a new Cursor throw another call
     * here.
     * <li> The Loader will release the data once it knows the application
     * is no longer using it.  For example, if the data is
     * a {@link Cursor} from a {@link CursorLoader},
     * you should not call close() on it yourself.  If the Cursor is being placed in a
     * {@link CursorAdapter}, you should use the
     * {@link CursorAdapter#swapCursor(Cursor)}
     * method so that the old Cursor is not closed.
     * </ul>
     *
     * @param loader The Loader that has finished.
     * @param cursor   The data generated by the Loader.
     */
    @SuppressWarnings({"JavadocReference", "JavaDoc"})
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
    }

    /**
     * Called when a previously created loader is being reset, and thus
     * making its data unavailable.  The application should at this point
     * remove any references it has to the Loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        mAdapter.swapCursor(null);
    }


    // --------------------------------------------------------------------
    // 壁紙変更時にブロードキャストレシーバーから叩かれるコールバック
    // --------------------------------------------------------------------

    @Override
    public void onWpChangeStart() {
        //// プログレスバー（グルグル）を表示する
        View progressView = this.findViewById(R.id.history_setWallpaper_progress);
        progressView.setVisibility(ProgressBar.VISIBLE);
    }

    @Override
    public void onWpChangeDone() {
        //// 履歴データを読み込み始める
        mLoaderManager.initLoader(1, null, this).forceLoad();

        //// プログレスバー（グルグル）を非表示にする
        View v = this.findViewById(R.id.history_setWallpaper_progress);
        v.setVisibility(ProgressBar.GONE);
    }

    @Override
    public void onWpChangeError() {
        Toast.makeText(this, R.string.main_toast_no_image, Toast.LENGTH_SHORT).show();
    }

}
