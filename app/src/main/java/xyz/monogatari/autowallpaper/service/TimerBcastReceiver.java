package xyz.monogatari.suke.autowallpaper.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


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
Log.d("○" + this.getClass().getSimpleName(), "電源ONになった瞬間のタイマー処理です");
            ((MainService)context).setTimer();
            ((MainService)context).cancelAlarm();

        } else if ( inttActionStr.equals(Intent.ACTION_SCREEN_OFF) ) {
Log.d("○" + this.getClass().getSimpleName(), "電源OFFになった瞬間のタイマー処理です");
            ((MainService)context).cancelTimer();
            ((MainService)context).setAlarm();
        }
    }
}
