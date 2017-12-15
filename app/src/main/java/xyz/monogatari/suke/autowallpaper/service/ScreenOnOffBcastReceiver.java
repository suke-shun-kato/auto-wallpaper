package xyz.monogatari.suke.autowallpaper.service;

import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.IOException;

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

            // --------------
            // セットする
            // --------------
            //// 画像取得

            ImgGetter imgGetter = new ImgGetterDir(context);
            Bitmap wallpaperBitmap = imgGetter.getImg();

            //// 画像加工


            //// 画像セット
            try {
                WallpaperManager wm = WallpaperManager.getInstance(context);
                int height = wm.getDesiredMinimumHeight();
                int width = wm.getDesiredMinimumWidth();
Log.d("○" + this.getClass().getSimpleName(), "height: " + height + ", width: " + width);

                if(Build.VERSION.SDK_INT >= 24){
                //APIレベル24以上の場合, Android7.0以上のとき
                    wm.setBitmap(
                            wallpaperBitmap,
                            null,
                            false,
                            WallpaperManager.FLAG_SYSTEM
                    );

                    wm.setBitmap(
                            wallpaperBitmap,
                            null,
                            false,
                            WallpaperManager.FLAG_LOCK
                    );
                } else {
                // 24未満のとき
                    wm.setBitmap(
                            wallpaperBitmap
                    );
                }
            } catch (IOException e) {
            }
        }
    }
}
