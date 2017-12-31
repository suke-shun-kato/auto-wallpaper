package xyz.monogatari.suke.autowallpaper.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import xyz.monogatari.suke.autowallpaper.util.ImgGetPorcSet;


/**
 * 電源ON,OFFのときのブロードキャストレシーバー
 * Created by k-shunsuke on 2017/12/14.
 */

public class TimerBcastReceiver extends BroadcastReceiver {
    /************************************
     * ブロードキャスト受信のタイミングで実行されるコールバック
     * ※別スレッドでブロードキャストレシーバーを登録しても、
     * プログラムが走るのはこのプログラムがある「「メインスレッド」」
     * @param context このレシーバーを登録した「アクティビティ」or「サービス」のコンテキスト
     * @param intent ブロードキャスト配信側から送られてきたインテント
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        // ----------------------------------
        // 例外処理
        // ----------------------------------
        //// nullのとき
        String inttActionStr = intent.getAction();
        if ( inttActionStr == null ) {
            return;
        }
        // ----------------------------------
        // メイン処理
        // ----------------------------------
        if ( inttActionStr.equals(Intent.ACTION_SCREEN_ON) ) {
//        AlarmManager alarmManager = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
//
//        if (Build.VERSION.SDK_INT <= 18) {   // ～Android 4.3
//            alarmManager.set(, , AlarmManager.RTC, );
//        } else if (19 <= Build.VERSION.SDK_INT && Build.VERSION.SDK_INT <= 22) {// Android4.4～Android 5.1
//            alarmManager.setExact();
//        } else if (23 <= Build.VERSION.SDK_INT ) {  // Android 6.0～
//            alarmManager.setExactAndAllowWhileIdle();
//        }


Log.d("○" + this.getClass().getSimpleName(), "電源ONになった瞬間のタイマー処理です");
            ((MainService)context).setTimer();

        } else if ( inttActionStr.equals(Intent.ACTION_SCREEN_OFF) ) {
Log.d("○" + this.getClass().getSimpleName(), "電源OFFになった瞬間のタイマー処理です");
            ((MainService)context).cancelTimer();
        }
    }
}
