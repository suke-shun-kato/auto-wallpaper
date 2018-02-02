package xyz.monogatari.suke.autowallpaper.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


import xyz.monogatari.suke.autowallpaper.MainActivity;
import xyz.monogatari.suke.autowallpaper.wpchange.WpManagerService;

/**
 *
 * Created by k-shunsuke on 2018/02/01.
 */
public class ProgressBcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        int stateInt = intent.getIntExtra(WpManagerService.KEY_NAME, WpManagerService.STATE_DESTROY);
        switch (stateInt) {
            case WpManagerService.STATE_START:
Log.d("○△" + this.getClass().getSimpleName(), "ブロードキャストれしーーーぶ:ON");
                ((MainActivity)context).onProgressVisible();
                break;
            case WpManagerService.STATE_DESTROY:
Log.d("○△" + this.getClass().getSimpleName(), "ブロードキャストれしーーーぶ:OFF");
                ((MainActivity)context).onProgressGone();
                break;

        }
    }
}
