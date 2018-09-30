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
import android.graphics.Point;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import xyz.monogatari.autowallpaper.HistoryActivity;
import xyz.monogatari.autowallpaper.MainActivity;
import xyz.monogatari.autowallpaper.NotificationChannelId;
import xyz.monogatari.autowallpaper.NotifyId;
import xyz.monogatari.autowallpaper.PendingIntentRequestCode;
import xyz.monogatari.autowallpaper.R;
import xyz.monogatari.autowallpaper.SettingsFragment;
import xyz.monogatari.autowallpaper.util.DisplaySizeCheck;
import xyz.monogatari.autowallpaper.util.MySQLiteOpenHelper;

/**
 * 壁紙周りの管理を行うクラス
 * Created by k-shunsuke on 2017/12/27.
 */
@SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
public class WpManager {
    // --------------------------------------------------------------------
    // フィールド
    // --------------------------------------------------------------------
    private final Context context;
    private final SharedPreferences sp;

    // --------------------------------------------------------------------
    // コンストラクタ
    // --------------------------------------------------------------------
    public WpManager(Context context) {
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
     * @param imgGetter 変更対象の画像のImgGetterクラス
     */
    private void insertHistories(SQLiteDatabase db, ImgGetter imgGetter) {
        // ----------------------------------
        // INSERT
        // ----------------------------------
        //// コード準備

        SQLiteStatement dbStt = db.compileStatement("" +
                "INSERT INTO histories (" +
                    "source_kind, img_uri, intent_action_uri, created_at" +
                ") VALUES ( ?, ?, ?, datetime('now') );");

        //// bind
        dbStt.bindString(1, imgGetter.getClass().getSimpleName() );
        dbStt.bindString(2, imgGetter.getImgUri());
        String uri = imgGetter.getActionUri();
        if (uri == null) {
            dbStt.bindNull(3);
        } else {
            dbStt.bindString(3, imgGetter.getActionUri());
        }

        //// insert実行
        dbStt.executeInsert();

    }
    private void deleteHistoriesOverflowMax(SQLiteDatabase db, @SuppressWarnings("SameParameterValue") int maxNum) {
        Cursor cursor = null;

        //noinspection TryFinallyCanBeTryWithResources
        try {
            cursor = db.rawQuery("SELECT count(*) AS count FROM histories", null);

            if (cursor != null && cursor.moveToFirst()) {
                int recordCount = cursor.getInt(cursor.getColumnIndexOrThrow("count"));
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
     * 壁紙が変更されたよという通知を送るメソッド
     *
     * @return boolean 通知送るのが成功したら true
     */
    private boolean sendNotification() {
        NotificationManager notifManager = (NotificationManager)this.context.getSystemService(Context.NOTIFICATION_SERVICE);
        if ( notifManager == null ) {
            return false;
        }


        // ----------
        // 通知チャンネルを作成
        // ----------
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){ //Android8.0（API 26）以上
            //// 通知チャンネルを作成→通知マネージャーに登録
            NotificationChannel ntfChannel = new NotificationChannel(
                    NotificationChannelId.WALLPAPER_CHANGED,
                    this.context.getString(R.string.histories_notification_ch_name),
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
                PendingIntentRequestCode.WALLPAPER_CHANGED,
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
                    NotificationChannelId.WALLPAPER_CHANGED
            );
        } else {
            //noinspection deprecation
            notifBuilder = new NotificationCompat.Builder(// この打ち消し線は問題ない
                    this.context
            );
        }
        notifBuilder.setSmallIcon(R.drawable.ic_notification_changed_wallpaper)
                .setAutoCancel(true)    //タップすると通知が消える
                .setContentTitle(this.context.getString(R.string.histories_notification_title))
                .setContentText(this.context.getString(R.string.histories_notification_body))
                .setContentIntent(pendingIntent)
                // 通知チャンネルをセット, Android8.0未満だとなにも処理しない
                .setChannelId(NotificationChannelId.WALLPAPER_CHANGED)
                .setWhen(System.currentTimeMillis());


        //// 通知をする
        Notification notification = notifBuilder.build();
        notifManager.notify(NotifyId.WALLPAPER_CHANGED, notification);

        // ----------
        //
        // ----------
        return true;
    }

    /**
     * 壁紙セットの一連の流れを実行するメソッド
     * 壁紙を取得→加工→壁紙セット→履歴に書き込み→通知作成
     * @param imgGetter 変更対象の画像のImgGetterクラス
     * @return 壁紙を設定できたか
     */
    public boolean executeWpSetTransaction(ImgGetter imgGetter) {
        // ----------------------------------
        // 画像取得
        // ----------------------------------
        Bitmap wallpaperBitmap = imgGetter.getImgBitmap(this.context); //データ本体取得

        // ----------------------------------
        // 画像加工
        // ----------------------------------
        // スクリーン（画面）サイズ取得
        Point point = DisplaySizeCheck.getRealSize(this.context);
        // 画像加工
        Bitmap processedWallpaperBitmap = BitmapProcessor.process(
                wallpaperBitmap, point.x, point.y,
                sp.getBoolean(SettingsFragment.KEY_OTHER_AUTO_ROTATION, true)
        );

        // ----------------------------------
        // 画像を壁紙にセット
        // ----------------------------------
        WallpaperManager wm = WallpaperManager.getInstance(this.context);
        try {
            if (Build.VERSION.SDK_INT >= 24) {
            // APIレベル24以上の場合, Android7.0以上のとき
                // 通常の壁紙とロックスクリーンの壁紙を変更
                wm.setBitmap(
                        processedWallpaperBitmap,
                        null,
                        false,
                        WallpaperManager.FLAG_SYSTEM | WallpaperManager.FLAG_LOCK
                );
            } else {
            // 24未満のとき
                // 通常の壁紙を変更
                wm.setBitmap(processedWallpaperBitmap);
            }
        } catch (IOException e) {
            return false;
        }


        // ----------------------------------
        // 履歴に書き込み
        // ----------------------------------
        MySQLiteOpenHelper mDbHelper = MySQLiteOpenHelper.getInstance(this.context);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        //noinspection TryFinallyCanBeTryWithResources
        try {
            this.insertHistories(db,imgGetter);
            // 記憶件数溢れたものを削除
            this.deleteHistoriesOverflowMax(db, HistoryActivity.MAX_RECORD_STORE);
        } finally {
            db.close();
        }

        // ----------------------------------
        // 通知を作成
        // ----------------------------------
        this.sendNotification();

        return true;
    }


    /************************************
     * 壁紙を取得→加工→セット する一連の流れを行う関数
     * 処理の都合上、別スレッドで壁紙をセットしないといけいないので直接使用は不可
     */
    public boolean executeWpSetRandomTransaction() {
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
        ImgGetter imgGetter = imgGetterList.get(drawnIndex);


        return executeWpSetTransaction(imgGetter);

    }
}
