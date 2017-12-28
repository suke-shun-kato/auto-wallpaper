package xyz.monogatari.suke.autowallpaper.util;

import android.Manifest;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.IOException;

import xyz.monogatari.suke.autowallpaper.service.ImgGetter;
import xyz.monogatari.suke.autowallpaper.service.ImgGetterDir;
import xyz.monogatari.suke.autowallpaper.service.ImgGetterTw;

/**
 * Created by k-shunsuke on 2017/12/27.
 */

public class ImgGetPorcSet {
    private Context context;

    public ImgGetPorcSet(Context context) {
        this.context = context;
    }

    public void getAndSetNewWallpaper() {
        // ----------------------------------
        // 画像取得
        // ----------------------------------

    if (true) {
        //// 例外処理、ストレージアクセスパーミッションがなければ途中で切り上げ
        if (ContextCompat.checkSelfPermission(this.context, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d("○" + this.getClass().getSimpleName(), "ストレージアクセス権限がない！！！");
            return;
        }
        //// メイン処理
        ImgGetter imgGetter = new ImgGetterDir(this.context);
        Bitmap wallpaperBitmap = imgGetter.getImg();
    }
////////////////////////////////////////////////////////////////////


        //// メイン処理
//        ImgGetter imgGetter = new ImgGetterTw(this.context);
//        Bitmap wallpaperBitmap = imgGetter.getImg();




        // ----------------------------------
        // 画像加工
        // ----------------------------------
Log.d("○" + this.getClass().getSimpleName(), "画像サイズ（加工前）: "
                + ", width:" + wallpaperBitmap.getWidth()
                + " height:" + wallpaperBitmap.getHeight());
        WallpaperManager wm = WallpaperManager.getInstance(this.context);

        // スクリーン（画面）サイズ取得
        Point point = DisplaySizeCheck.getRealSize(this.context);
        // 画像加工
        Bitmap processedWallpaperBitmap = BitmapProcessor.process(
                wallpaperBitmap, point.x, point.y,
                PreferenceManager.getDefaultSharedPreferences(this.context)
                        .getBoolean("other_autoRotation", true)
        );

Log.d("○" + this.getClass().getSimpleName(), "画像サイズ（加工後）: "
                + ", width:" + processedWallpaperBitmap.getWidth()
                + " height:" + processedWallpaperBitmap.getHeight());
Log.d("○" + this.getClass().getSimpleName(), "ディスプレイサイズ: "
                + " width: " + point.x
                + ", height: " + point.y);

        // ----------------------------------
        // 画像セット
        // ----------------------------------
        try {
            if (Build.VERSION.SDK_INT >= 24) {
                //APIレベル24以上の場合, Android7.0以上のとき
                wm.setBitmap(
                        processedWallpaperBitmap,
                        null,
                        false,
                        WallpaperManager.FLAG_SYSTEM
                );
                wm.setBitmap(
                        processedWallpaperBitmap,
                        null,
                        false,
                        WallpaperManager.FLAG_LOCK
                );
            } else {
                // 24未満のとき
                wm.setBitmap(processedWallpaperBitmap);
            }
        } catch (IOException e) {
Log.d("○" + this.getClass().getSimpleName(), "壁紙セットできません");
        }
    }
}
