package xyz.goodistory.autowallpaper.wpchange;

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
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import xyz.goodistory.autowallpaper.HistoryActivity;
import xyz.goodistory.autowallpaper.HistoryModel;
import xyz.goodistory.autowallpaper.MainActivity;
import xyz.goodistory.autowallpaper.PendingIntentRequestCode;
import xyz.goodistory.autowallpaper.R;
import xyz.goodistory.autowallpaper.SettingsFragment;
import xyz.goodistory.autowallpaper.util.DisplaySizeCheck;
import xyz.goodistory.autowallpaper.util.MySQLiteOpenHelper;

/**
 * 壁紙周りの管理を行うクラス
 * Created by k-shunsuke on 2017/12/27.
 */
@SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
public class WpManager {
    // --------------------------------------------------------------------
    // フィールド
    // --------------------------------------------------------------------
    private final Context mContext;
    private final SharedPreferences mSp;

    // --------------------------------------------------------------------
    // コンストラクタ
    // --------------------------------------------------------------------
    public WpManager(Context context) {
        mContext = context;
        mSp = PreferenceManager.getDefaultSharedPreferences(context);
    }

    // --------------------------------------------------------------------
    // メソッド
    // --------------------------------------------------------------------

    /************************************
     * 壁紙が変更されたよという通知を送るメソッド
     *
     * @return boolean 通知送るのが成功したら true
     */
    private boolean sendNotification() {
        NotificationManager notifManager = (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if ( notifManager == null ) {
            return false;
        }

        String notificationChannelId = mContext.getResources()
                .getString(R.string.id_notificationChannel_wallpaperChanged);

        // ----------
        // 通知チャンネルを作成
        // ----------
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){ //Android8.0（API 26）以上
            //// 通知チャンネルを作成→通知マネージャーに登録
            NotificationChannel ntfChannel = new NotificationChannel(
                    notificationChannelId,
                    mContext.getString(R.string.histories_notification_ch_name),
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            //// マネージャーに登録
            notifManager.createNotificationChannel(ntfChannel);
        }


        // ----------
        // PendingIntentを作成する
        // ----------
        Intent mainIntent = new Intent(mContext, MainActivity.class)
                // FLAG_ACTIVITY_NEW_TASK: スタックに残っていても、新しくタスクを起動させる
                // FLAG_ACTIVITY_CLEAR_TOP：呼び出すActivity以外のActivityをクリアして起動させる
                // 上記はセットで使うのが基本みたい
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        Intent historyIntent = new Intent(mContext, HistoryActivity.class);
        Intent[] intents = {mainIntent, historyIntent};
        PendingIntent pendingIntent = PendingIntent.getActivities(
                mContext,
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
            notifBuilder = new NotificationCompat.Builder(mContext,  notificationChannelId);
        } else {
            //noinspection deprecation
            notifBuilder = new NotificationCompat.Builder(mContext);
        }
        notifBuilder.setSmallIcon(R.drawable.ic_notification_changed_wallpaper)
                .setAutoCancel(true)    //タップすると通知が消える
                .setContentTitle(mContext.getString(R.string.histories_notification_title))
                .setContentText(mContext.getString(R.string.histories_notification_body))
                .setContentIntent(pendingIntent)
                // 通知チャンネルをセット, Android8.0未満だとなにも処理しない
                .setChannelId(notificationChannelId)
                .setWhen(System.currentTimeMillis());


        //// 通知をする
        Notification notification = notifBuilder.build();
        int notificationId = mContext
                .getResources()
                .getInteger(R.integer.id_notification_wallpaperChanged);
        notifManager.notify(notificationId, notification);

        // ----------
        //
        // ----------
        return true;
    }

    /**
     * 壁紙セットの一連の流れを実行するメソッド
     * 壁紙を取得→加工→壁紙セット→履歴に書き込み→通知作成
     * @param imgGetter 変更対象の画像のImgGetterクラス
     */
    public void executeWpSetProcess(ImgGetter imgGetter) throws Exception {
        // ----------------------------------
        // 画像取得
        // ----------------------------------
        Bitmap wallpaperBitmap = imgGetter.getImgBitmapWhenErrorFromDevice(mContext); //データ本体取得
        if (wallpaperBitmap == null) {
            throw new RuntimeException("画像を取得できませんでした。");
        }

        // ----------------------------------
        // 画像加工
        // ----------------------------------
        // スクリーン（画面）サイズ取得
        Point point = DisplaySizeCheck.getRealSize(mContext);
        // 画像加工
        Bitmap processedWallpaperBitmap = BitmapProcessor.process(
                wallpaperBitmap, point.x, point.y,
                mSp.getBoolean(SettingsFragment.KEY_OTHER_AUTO_ROTATION, true)
        );

        // ----------------------------------
        // 画像を壁紙にセット
        // ----------------------------------
        WallpaperManager wm = WallpaperManager.getInstance(mContext);

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

        // ----------------------------------
        // 履歴に書き込み & 画像に保存
        // ----------------------------------
        //// 変数の準備
        Map<String, String> paramsHistoryMap = imgGetter.getAll();

        HistoryModel historyMdl = new HistoryModel(mContext);
        SQLiteDatabase db = MySQLiteOpenHelper.getInstance(mContext).getWritableDatabase();

        //// DBに書き込み
        db.beginTransaction();
        String deviceImgFileName = imgGetter.generateDeviceImgName();
        try {
            // histories に保存 & 画像も保存
            historyMdl.insertAndSaveBitmap(
                    paramsHistoryMap, wallpaperBitmap, deviceImgFileName);

            // 記憶件数溢れたものを削除
            historyMdl.deleteHistoriesOverflowMax(HistoryActivity.MAX_RECORD_STORE);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            historyMdl.deleteImg( deviceImgFileName );
            throw e; // ここでthrow してもfinallyは実行される
        } finally {
            db.endTransaction();
            historyMdl.close();
        }

        // ----------------------------------
        // 通知を作成
        // ----------------------------------
        this.sendNotification();
    }


    /************************************
     * 壁紙を取得→加工→セット する一連の流れを行う関数
     * 処理の都合上、別スレッドで壁紙をセットしないといけいないので直接使用は不可
     */
    public void executeWpRandomSetProcess() throws Exception {
        // ----------------------------------
        // 画像取得
        // 取得元の選択が複数あるときは等確率で抽選を行う
        // ----------------------------------
        // ----------
        // 画像リストを取得
        // ----------
        //// 抽選先の取得リストをListに入れる
        List<ImgGetter> imgGetterList = new ArrayList<>();

        // ディレクトリ
        if (mSp.getBoolean(SettingsFragment.KEY_FROM_DIR, false)
                && ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            imgGetterList.addAll( ImgGetterDir.getImgGetterList(mContext) );
        }

        // twitter
        if (mSp.getBoolean(SettingsFragment.KEY_FROM_TWITTER_FAV, false)
                && mSp.getString(SettingsFragment.KEY_FROM_TWITTER_OAUTH, null) != null) {
            imgGetterList.addAll( ImgGetterTw.getImgGetterList(mContext) );
        }

        // Instagram
        String keyFromInstagramUserRecent
                = mContext.getString(R.string.preference_key_from_instagram_user_recent);
        String keyAuthenticateInstagram
                =  mContext.getString(R.string.preference_key_authenticate_instagram);
        if ( mSp.getBoolean(keyFromInstagramUserRecent, false)
                && mSp.getString(keyAuthenticateInstagram, null) != null)  {
            List<ImgGetter> imgGetters = (new WpUrisGetterInstagram(mContext)).getImgGetterList();
            imgGetterList.addAll(imgGetters);
        }

        // ----------
        // 抽選
        // ----------
        if (imgGetterList.size() == 0) {
            throw new RuntimeException("ランダム変更対象の壁紙がありません。");
        }
        int drawnIndex = new Random().nextInt(imgGetterList.size());
        ImgGetter imgGetter = imgGetterList.get(drawnIndex);


        // ----------
        // 実行
        // ----------
        executeWpSetProcess(imgGetter);
    }
}
