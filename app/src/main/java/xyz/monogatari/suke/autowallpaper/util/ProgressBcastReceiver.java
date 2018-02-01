package xyz.monogatari.suke.autowallpaper.util;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import xyz.monogatari.suke.autowallpaper.MainActivity;

/**
 * Created by k-shunsuke on 2018/02/01.
 */

public class ProgressBcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
Log.d("○△" + this.getClass().getSimpleName(), "ブロードキャストれしーーーぶ");

        int stateInt = intent.getIntExtra(WpManagerService.KEY_NAME, WpManagerService.STATE_DESTROY);
        switch (stateInt) {
            case WpManagerService.STATE_START:
                ((MainActivity)context).onProgressVisible();
                break;
            case WpManagerService.STATE_DESTROY:
                ((MainActivity)context).onProgressGone();
                break;

        }
    }
}
