package xyz.goodistory.autowallpaper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import xyz.goodistory.autowallpaper.wpchange.WpManagerService;


public class TimerWpChangeReceiver extends BroadcastReceiver {
    /** 呼び出し元判定のためのリクエストコード、実際にはこれ一つだけだが定義 */
    public static final int REQUEST_CODE_MAIN_SERVICE = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        WpManagerService.changeWpRandom(context);
    }
}