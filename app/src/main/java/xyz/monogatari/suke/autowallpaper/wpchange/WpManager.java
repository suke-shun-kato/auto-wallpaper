package xyz.monogatari.suke.autowallpaper.wpchange;

import android.Manifest;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import xyz.monogatari.suke.autowallpaper.SettingsFragment;
import xyz.monogatari.suke.autowallpaper.util.DisplaySizeCheck;
import xyz.monogatari.suke.autowallpaper.util.FeedReaderDbHelper;

/**
 * 壁紙を取得→加工→セットまでの一連の流れを行うクラス
 * Created by k-shunsuke on 2017/12/27.
 */
@SuppressWarnings("WeakerAccess")
public class WpManager {
    private final Context context;
    private final SharedPreferences sp;
    private ImgGetter imgGetter;

    public WpManager(Context context) {
        this.context = context;
        this.sp = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /************************************
     * データベースをに
     */
    public void insertHistory() {
        FeedReaderDbHelper mDbHelper = new FeedReaderDbHelper(this.context);
//this.imgGetter のクラス名
//this.imgGetter.getActionUri()
//this.imgGetter.getImgUri()
        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        try {
            db.execSQL("INSERT INTO histories (source_kind, intent_action_uri, img_uri, created_at) VALUES (1, 'https://pbs.twimg.com/media/DVOb69UVwAATora.jpg', 'https://twitter.com/YSD0118/status/960282606796525569', datetime('now'));");
        } finally {
            db.close();
        }
    }

    /************************************
     * 壁紙を取得→加工→セット する一連の流れを行う関数
     * 処理の都合上、別スレッドで壁紙をセットしないといけいないので直接使用は不可
     */
    public void execute() {
        // ----------------------------------
        // 画像取得
        // 取得元の選択が複数あるときは等確率で抽選を行う
        // ----------------------------------
        // ----------
        // どこから画像を取得するか抽選
        // ----------
        //// 抽選先の取得リストをListに入れる
        List<String> drawnList = new ArrayList<>();
        if (sp.getBoolean(SettingsFragment.KEY_FROM_DIR, false)
                && ContextCompat.checkSelfPermission(this.context, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            drawnList.add(SettingsFragment.KEY_FROM_DIR);
        }
        if (sp.getBoolean(SettingsFragment.KEY_FROM_TWITTER_FAV, false)
                && sp.getString(SettingsFragment.KEY_FROM_TWITTER_OAUTH, null) != null) {
            drawnList.add(SettingsFragment.KEY_FROM_TWITTER_FAV);
        }

        //// 抽選
        if (drawnList.size() == 0) {
            return;
        }
        int drawnIndex = new Random().nextInt(drawnList.size());
        String selectedStr = drawnList.get(drawnIndex);

        // ----------
        // 画像を取得
        // ----------
        //// imgGetterを取得
        switch(selectedStr) {
            case SettingsFragment.KEY_FROM_DIR:
                //// 例外処理、ストレージアクセスパーミッションがなければ途中で切り上げ
                if (ContextCompat.checkSelfPermission(this.context, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    Log.d("○" + this.getClass().getSimpleName(), "ストレージアクセス権限がない！！！");
                    return;
                }
                this.imgGetter = new ImgGetterDir(this.context);
                break;
            case SettingsFragment.KEY_FROM_TWITTER_FAV:
                this.imgGetter = new ImgGetterTw(this.context);
                break;
            default:
                // 途中で切り上げ、何もしない
                return;
        }

        //// 壁紙を取得
//        Bitmap wallpaperBitmap = this.imgGetter.getImg();

        this.imgGetter.drawImg();////ここでURIなどを記録
        String imgUri = this.imgGetter.getImgUri();
        String actionUri = this.imgGetter.getActionUri();
        Bitmap wallpaperBitmap = this.imgGetter.getImgBitmap();
Log.d("○○○○○○"+this.getClass().getSimpleName(), "imgUri:"+imgUri + ", actionUri:"+actionUri);

        // todo ↓の取得できなかったときのエラーハンドリングをちゃんとする、ディレクトリにファイルゼロやTwitterのアクセス制限など
        if (wallpaperBitmap == null) {
            Toast.makeText(this.context, "画像取得エラー", Toast.LENGTH_SHORT).show();
            return;
        }


        // ----------------------------------
        // 画像加工
        // ----------------------------------
Log.d("○" + this.getClass().getSimpleName(), "画像サイズ（加工前）: "
+ ", width:" + wallpaperBitmap.getWidth()
+ " height:" + wallpaperBitmap.getHeight());

        // スクリーン（画面）サイズ取得
        Point point = DisplaySizeCheck.getRealSize(this.context);
        // 画像加工
        Bitmap processedWallpaperBitmap = BitmapProcessor.process(
                wallpaperBitmap, point.x, point.y,
                sp.getBoolean(SettingsFragment.KEY_OTHER_AUTO_ROTATION, true)
        );

Log.d("○" + this.getClass().getSimpleName(), "画像サイズ（加工後）: "
                + ", width:" + processedWallpaperBitmap.getWidth()
                + " height:" + processedWallpaperBitmap.getHeight());
Log.d("○" + this.getClass().getSimpleName(), "ディスプレイサイズ: "
                + " width: " + point.x
                + ", height: " + point.y);

        // ----------------------------------
        // 画像を壁紙にセット
        // ----------------------------------
        WallpaperManager wm = WallpaperManager.getInstance(this.context);
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
