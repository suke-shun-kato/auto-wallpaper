package xyz.monogatari.suke.autowallpaper.wpchange;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import xyz.monogatari.suke.autowallpaper.HistoryActivity;
import xyz.monogatari.suke.autowallpaper.R;
import xyz.monogatari.suke.autowallpaper.SettingsFragment;
import xyz.monogatari.suke.autowallpaper.util.DisplaySizeCheck;
import xyz.monogatari.suke.autowallpaper.util.MySQLiteOpenHelper;

/**
 * 壁紙を取得→加工→セットまでの一連の流れを行うクラス
 * Created by k-shunsuke on 2017/12/27.
 */
@SuppressWarnings("WeakerAccess")
public class WpManager {
    // --------------------------------------------------------------------
    // フィールド
    // --------------------------------------------------------------------
    private final Context context;
    private final SharedPreferences sp;
    private ImgGetter imgGetter = null;
//    private final Map<String, Integer> sourceKindMap = new HashMap<>();

    // --------------------------------------------------------------------
    // 定数
    // --------------------------------------------------------------------
    /** ひとまずnotification id を定義、これしかないので実質意味がないが・・・ */
    public static final int NOTIFICATION_ID_NORMAL = 1;

    // --------------------------------------------------------------------
    // コンストラクタ
    // --------------------------------------------------------------------
    public WpManager(Context context) {
//        // ----------------------------------
//        // クラス名→DBのsource_kind変換用のハッシュマップの作成
//        // ----------------------------------
//        this.sourceKindMap.put("ImgGetterDir", 1);
//        this.sourceKindMap.put("ImgGetterTw", 2);


        // ----------------------------------
        //
        // ----------------------------------
        this.context = context;
        this.sp = PreferenceManager.getDefaultSharedPreferences(context);
    }

    // --------------------------------------------------------------------
    // メソッド
    // CREATE INDEX created_at ON histories(created_at);
    // --------------------------------------------------------------------
    /************************************
     * データベースを履歴を記録
     * @param db 書き込み先のdbオブジェクト
     */
    private void insertHistories(SQLiteDatabase db) {
        //noinspection TryFinallyCanBeTryWithResources
//        try {
            // ----------------------------------
            // INSERT
            // ----------------------------------
            //// コード準備
            // ↓のコードでInspectionエラーが出るがAndroidStudioのバグなので放置、3.1では直るらしい

            SQLiteStatement dbStt = db.compileStatement("" +
                    "INSERT INTO histories (" +
                        "source_kind, img_uri, intent_action_uri, created_at" +
                    ") VALUES ( ?, ?, ?, datetime('now') );");

            //// bind
Log.d("○○○"+this.getClass().getSimpleName(), "imgGetterのクラス名は！:"+this.imgGetter.getClass().getSimpleName());
            dbStt.bindString(1, this.imgGetter.getClass().getSimpleName() );
            dbStt.bindString(2, this.imgGetter.getImgUri());
            String uri = this.imgGetter.getActionUri();
            if (uri == null) {
                dbStt.bindNull(3);
            } else {
                dbStt.bindString(3, this.imgGetter.getActionUri());
            }

            //// insert実行
            dbStt.executeInsert();

//
//        } finally {
//            db.close();
//        }
    }
    private void deleteHistoriesOverflowMax(SQLiteDatabase db, @SuppressWarnings("SameParameterValue") int maxNum) {
        Cursor cursor = null;

        try {
            cursor = db.rawQuery("SELECT count(*) AS count FROM histories", null);

            if (cursor != null && cursor.moveToFirst()) {
                int recordCount = cursor.getInt(cursor.getColumnIndexOrThrow("count"));
Log.d("○"+this.getClass().getSimpleName(), "count: " + recordCount);
                if (recordCount > maxNum) {
                    SQLiteStatement dbStt = db.compileStatement(
                            "DELETE FROM histories WHERE created_at IN (" +
                                    "SELECT created_at FROM histories ORDER BY created_at ASC LIMIT ?)"
                    );
                    dbStt.bindLong(1, recordCount - maxNum);
                    dbStt.executeUpdateDelete();
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }


    /************************************
     * 壁紙を取得→加工→セット する一連の流れを行う関数
     * 処理の都合上、別スレッドで壁紙をセットしないといけいないので直接使用は不可
     */
    public boolean execute() {
        // ----------------------------------
        // 画像取得
        // 取得元の選択が複数あるときは等確率で抽選を行う
        // ----------------------------------
        // ----------
        // 画像リストを取得
        // ----------
        //// 抽選先の取得リストをListに入れる
        List<ImgGetter> imgGetterList = new ArrayList<>();
        if (sp.getBoolean(SettingsFragment.KEY_FROM_DIR, false)
                && ContextCompat.checkSelfPermission(this.context, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            imgGetterList.addAll( ImgGetterDir.getImgGetterList(this.context) );
        }
        if (sp.getBoolean(SettingsFragment.KEY_FROM_TWITTER_FAV, false)
                && sp.getString(SettingsFragment.KEY_FROM_TWITTER_OAUTH, null) != null) {
            imgGetterList.addAll( ImgGetterTw.getImgGetterList(this.context) );
        }

        // ----------
        // 抽選
        // ----------
        if (imgGetterList.size() == 0) {
            return false;
        }
        int drawnIndex = new Random().nextInt(imgGetterList.size());
        this.imgGetter = imgGetterList.get(drawnIndex);

        // ----------
        // 画像取得
        // ----------
        Bitmap wallpaperBitmap = this.imgGetter.getImgBitmap(this.context); //データ本体取得

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


        // ----------------------------------
        // 履歴に書き込み
        // ----------------------------------
        MySQLiteOpenHelper mDbHelper = new MySQLiteOpenHelper(this.context);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        //noinspection TryFinallyCanBeTryWithResources
        try {
            this.insertHistories(db);
            // 記憶件数溢れたものを削除
            this.deleteHistoriesOverflowMax(db, HistoryActivity.MAX_RECORD_STORE);
        } finally {
            db.close();
        }

        // ----------------------------------
        // 通知を作成
        // ----------------------------------
        Notification notification = new Notification.Builder(this.context)
                .setAutoCancel(true)    //タップすると通知が消える
                .setContentTitle(this.context.getString(R.string.histories_notification_title))
                .setContentText(this.context.getString(R.string.histories_notification_body))
                .setSmallIcon(R.drawable.ic_notifications_active_black_24dp)//todo ちゃんとする
                .setWhen(System.currentTimeMillis())
                .setVibrate(new long[]{1000, 500})  //1秒後に0.5秒だけ振動
                .setLights(Color.BLUE,2000,1000)    //2秒ON→1秒OFF→2秒ONを繰り返す
                .setContentIntent(
                        PendingIntent.getActivity(
                                this.context,
                                HistoryActivity.REQUEST_CODE_NORMAL,    // リクエストコード
                                new Intent(this.context, HistoryActivity.class),
                                PendingIntent.FLAG_UPDATE_CURRENT   //PendingIntentオブジェクトが既にあったらそのまま、ただしextraの値は最新に更新される
                        )
                )
                .build();   //todo ビルダーの設定で良い設定がないか確認する

        NotificationManager nManager
                = (NotificationManager) this.context.getSystemService(Context.NOTIFICATION_SERVICE);
        try {
            if ( nManager != null ) {
                nManager.notify(NOTIFICATION_ID_NORMAL, notification);
            }
        } catch(NullPointerException e) {
            e.printStackTrace();
        }

        // ----------------------------------
        //
        // ----------------------------------
        return true;
    }
}
