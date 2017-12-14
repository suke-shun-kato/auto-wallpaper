package xyz.monogatari.suke.autowallpaper.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * 電源ON,OFFのときのブロードキャストレシーバー
 * Created by k-shunsuke on 2017/12/14.
 */

public class ScreenOnOffBcastReceiver extends BroadcastReceiver {
    /************************************
     * ブロードキャスト受信のタイミングで実行されるコールバック
     * @param context このレシーバーを登録した「アクティビティ」or「サービス」のコンテキスト
     * @param intent ブロードキャスト配信側から送られてきたインテント
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        // ----------------------------------
        // 例外処理
        // ----------------------------------
        //// 電源ON時に壁紙入れ替えする設定がOFFのとき処理を行わないで切り上げ
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        if ( !sp.getBoolean("when_turnOn", false) ) {
            return;
        }

        //// nullのとき
        String inttActionStr = intent.getAction();
        if ( inttActionStr == null ) {
            return;
        }
        // ----------------------------------
        // メイン処理
        // ----------------------------------
        if ( inttActionStr.equals(Intent.ACTION_SCREEN_ON) ) {
Log.d("○" + this.getClass().getSimpleName(), "電源ONになった瞬間の壁紙処理");
        } else if ( inttActionStr.equals(Intent.ACTION_SCREEN_OFF) ) {
Log.d("○" + this.getClass().getSimpleName(), "電源OFFになった瞬間の壁紙処理");
        }
    }
}
