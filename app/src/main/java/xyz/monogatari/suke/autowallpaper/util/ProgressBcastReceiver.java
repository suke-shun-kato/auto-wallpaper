package xyz.monogatari.suke.autowallpaper.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;


import xyz.monogatari.suke.autowallpaper.MainActivity;
import xyz.monogatari.suke.autowallpaper.R;
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
Log.d("○" + this.getClass().getSimpleName(), "ブロードキャストれしーーーぶ:ON");
                ((MainActivity)context).onProgressVisible();
                break;
            case WpManagerService.STATE_DESTROY:
Log.d("○" + this.getClass().getSimpleName(), "ブロードキャストれしーーーぶ:OFF");
                ((MainActivity)context).onProgressGone();
Log.d("○" + this.getClass().getSimpleName(), "ブロードキャストれしーーーぶ:OFF2");
                break;
            case WpManagerService.STATE_ERROR:
Log.d("○" + this.getClass().getSimpleName(), "ブロードキャストれしーーーぶERROR");

                Toast.makeText(context, R.string.main_toast_no_image, Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
