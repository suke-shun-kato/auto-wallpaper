package xyz.monogatari.autowallpaper.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import xyz.monogatari.autowallpaper.wpchange.WpManagerService;

/**
 *
 * Created by k-shunsuke on 2018/02/01.
 */
public class ProgressBcastReceiver extends BroadcastReceiver {

    /**
     * 壁紙変更状態を伝えるリスナー、主にアクティビティでimplementして作るよう
     */
    public interface OnStateChangeListener{
        void onWpChangeStart();
        void onWpChangeDone();
        void onWpChangeError();
    }


    /**
     * 壁紙変更IntentServiceから送るブロードキャストのレシーバー
     * @param context レシーバーを設置するクラスのcontext
     * @param intent 送られてくるintent
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        int stateInt = intent.getIntExtra(WpManagerService.EXTRA_WP_STATE, WpManagerService.WP_STATE_DONE);
        switch (stateInt) {
            case WpManagerService.WP_STATE_CHANGING:
//Log.d("○" + this.getClass().getSimpleName(), "ブロードキャストれしーーーぶ:ON:" + context.getClass().getName());
                ((OnStateChangeListener)context).onWpChangeStart();
                break;

            case WpManagerService.WP_STATE_DONE:
//Log.d("○" + this.getClass().getSimpleName(), "ブロードキャストれしーーーぶ:OFF");
                ((OnStateChangeListener)context).onWpChangeDone();
                break;

            case WpManagerService.WP_STATE_ERROR:
//Log.d("○" + this.getClass().getSimpleName(), "ブロードキャストれしーーーぶERROR");
                ((OnStateChangeListener)context).onWpChangeError();
                break;
        }
    }
}
