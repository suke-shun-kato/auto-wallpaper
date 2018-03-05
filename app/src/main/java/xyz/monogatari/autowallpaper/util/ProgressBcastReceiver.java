package xyz.monogatari.autowallpaper.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


import xyz.monogatari.autowallpaper.MainActivity;
import xyz.monogatari.autowallpaper.wpchange.WpManagerService;

/**
 *
 * Created by k-shunsuke on 2018/02/01.
 */
public class ProgressBcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        int stateInt = intent.getIntExtra(WpManagerService.KEY_NAME, WpManagerService.STATE_DESTROY);
        switch (stateInt) {
            case WpManagerService.STATE_ON:
Log.d("○" + this.getClass().getSimpleName(), "ブロードキャストれしーーーぶ:ON");
                ((MainActivity)context).onWpChanging();
                break;

            case WpManagerService.STATE_DESTROY:
Log.d("○" + this.getClass().getSimpleName(), "ブロードキャストれしーーーぶ:OFF");
                ((MainActivity)context).onWpChangeDone();
                break;

            case WpManagerService.STATE_ERROR:
Log.d("○" + this.getClass().getSimpleName(), "ブロードキャストれしーーーぶERROR");
                ((MainActivity)context).onWpChangeError();
                break;
        }
    }
}
