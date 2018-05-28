package xyz.monogatari.autowallpaper.wpchange;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
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
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import xyz.monogatari.autowallpaper.HistoryActivity;
import xyz.monogatari.autowallpaper.MainActivity;
import xyz.monogatari.autowallpaper.NotifyId;
import xyz.monogatari.autowallpaper.PendingIntentRequestCode;
import xyz.monogatari.autowallpaper.R;
import xyz.monogatari.autowallpaper.SettingsFragment;
import xyz.monogatari.autowallpaper.util.DisplaySizeCheck;
import xyz.monogatari.autowallpaper.util.MySQLiteOpenHelper;

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
        NotificationManager notifManager = (NotificationManager)this.context.getSystemService(Context.NOTIFICATION_SERVICE);
        if ( notifManager == null ) {
            return true;
        }


        // ----------
        // 通知チャンネルを作成
        // ----------
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){ //Android8.0（API 26）以上
            //// 通知チャンネルを作成→通知マネージャーに登録
            String channelName = "namanameaname";   //TODO XMLから取得するようにする
            NotificationChannel ntfChannel = new NotificationChannel(
                    this.context.getString(R.string.notification_ch_id_wp_change),
                    channelName,
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            //// マネージャーに登録
            notifManager.createNotificationChannel(ntfChannel);
        }


        // ----------
        // PendingIntentを作成する
        // ----------
        Intent mainIntent = new Intent(this.context, MainActivity.class)
                // FLAG_ACTIVITY_NEW_TASK: スタックに残っていても、新しくタスクを起動させる
                // FLAG_ACTIVITY_CLEAR_TOP：呼び出すActivity以外のActivityをクリアして起動させる
                // 上記はセットで使うのが基本みたい
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        Intent historyIntent = new Intent(this.context, HistoryActivity.class);
        Intent[] intents = {mainIntent, historyIntent};
        PendingIntent pendingIntent = PendingIntent.getActivities(
                this.context,
                PendingIntentRequestCode.WALLPAPER_CHANGED,    // リクエストコード TODO XMLに移す
                intents,
                //PendingIntentオブジェクトが既にあったらそのまま、ただしextraの値は最新に更新される
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        // ----------
        // 通知をする
        // ----------
        //// 通知ビルダーを作成
        NotificationCompat.Builder notifBuilder;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {   //Android8.0（API 26）以上
            notifBuilder = new NotificationCompat.Builder(
                    this.context,
                    this.context.getString(R.string.notification_ch_id_wp_change)
            );
        } else {
            notifBuilder = new NotificationCompat.Builder(// この打ち消し線は問題ない
                    this.context
            );
        }
        notifBuilder.setSmallIcon(R.drawable.ic_notification_changed_wallpaper)
                .setAutoCancel(true)    //タップすると通知が消える
                .setContentTitle(this.context.getString(R.string.histories_notification_title))
                .setContentText(this.context.getString(R.string.histories_notification_body))
                .setContentIntent(pendingIntent)



                .setWhen(System.currentTimeMillis())
                .setVibrate(new long[]{1000, 500})  //1秒後に0.5秒だけ振動
                //2秒ON→1秒OFF→2秒ONを繰り返す
                .setLights(Color.BLUE,2000,1000) ;


        //// 通知をする
        Notification notification = notifBuilder.build();
        notifManager.notify(NotifyId.WALLPAPER_CHANGED, notification);  //TODO NotifyIdをXMLに移す


        return true;
    }
}
