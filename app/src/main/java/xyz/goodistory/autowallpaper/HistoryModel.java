package xyz.goodistory.autowallpaper;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;

import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import xyz.goodistory.autowallpaper.util.MySQLiteOpenHelper;

@SuppressWarnings("WeakerAccess")
public class HistoryModel {
    // --------------------------------------------------------------------
    //
    // --------------------------------------------------------------------
    private final SQLiteDatabase mDbReadable;
    private final SQLiteDatabase mDbWritable;
    private final Context mContext;

    public static final String TABLE_NAME = "histories";
    public static final String[] PROJECTION = new String[] {
            "_id",
            "source_kind",
            "img_uri",
            "intent_action_uri",
            "created_at",
            "device_img_uri"
    };

    public static final String SOURCE_TW = "ImgGetterTw";
    public static final String SOURCE_DIR = "ImgGetterDir";
    public static final String SOURCE_SHARE = "share";
    public static final Set<String> SOURCE_KINDS = new HashSet<>();
    static {
        SOURCE_KINDS.add(SOURCE_TW);
        SOURCE_KINDS.add(SOURCE_DIR);
        SOURCE_KINDS.add(SOURCE_SHARE);
    }

    public static final Map<String, Integer> ICON_R_IDS = new HashMap<>();
    static {
        ICON_R_IDS.put(SOURCE_DIR, R.drawable.ic_dir);
        ICON_R_IDS.put(SOURCE_TW, R.drawable.ic_twitter);
        ICON_R_IDS.put(SOURCE_SHARE, R.drawable.ic_share);
    }

    // --------------------------------------------------------------------
    // 
    // --------------------------------------------------------------------
    public HistoryModel(Context context) {
        mContext = context;

        MySQLiteOpenHelper dbHelper = MySQLiteOpenHelper.getInstance(context);
        mDbReadable = dbHelper.getReadableDatabase();
        mDbWritable = dbHelper.getWritableDatabase();
    }

    public void close() {
        mDbReadable.close();
        mDbWritable.close();
    }
    // --------------------------------------------------------------------
    // 
    // --------------------------------------------------------------------
    /**
     * idを指定して履歴データを取得する
     * @param id id
     * @return cursor
     */
    public Cursor getHistoryById(long id) {
        return mDbReadable.query(
                TABLE_NAME,
                PROJECTION,
                "_id=?",
                new String[] {String.valueOf(id)},
                null, null, null);
    }

    /**
     * 全ての履歴データを取得する
     * @return 履歴データ
     */
    public Cursor getAllHistories() {
        return mDbReadable.query(
                TABLE_NAME,
                PROJECTION,
                null, null, null, null,
                "created_at DESC",
                String.valueOf(HistoryActivity.MAX_RECORD_STORE));
    }

    /**
     * Bitmapをアプリ内ストレージにPNGで保存する
     * @param bitmap 保存するファイル
     * @param fileName 保存するファイル名, 拡張子なし
     * @throws Exception FileNotFountException, IOException
     */
    public void saveImg(Bitmap bitmap, String fileName) throws Exception {
        //// ファイルの拡張子がpngかどうかチェック
        int i = fileName.lastIndexOf('.');
        if (i < 0 || !fileName.substring(i + 1).equals("png")) {
            throw new RuntimeException("ファイルの拡張子はpngにしてください。");
        }

        //// 画像を保存
        FileOutputStream fileOutputStream = mContext.openFileOutput(fileName, Context.MODE_PRIVATE);
        // 保存、第二引数は圧縮度、pngはなんでもよいらしいが100にしている
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
        fileOutputStream.close();
    }

    public void deleteImg(String fileName) {
        mContext.deleteFile(fileName);
    }

    public void deleteImgs(String[] fileNames) {
        for (String fileName: fileNames) {
            deleteImg(fileName);
        }
    }
    /**
     * 壁紙の履歴をDBに登録する
     * @param insertParams 登録対象
     */
    public void insert(Map<String, String> insertParams) {
        // ----------------------------------
        // INSERT
        // ----------------------------------
        //// コード準備

        SQLiteStatement dbStt = mDbWritable.compileStatement("" +
                "INSERT INTO histories (" +
                "source_kind, img_uri, intent_action_uri, created_at, device_img_uri" +
                ") VALUES ( ?, ?, ?, datetime('now'), ? );");

        //// bind
        dbStt.bindString(1, insertParams.get("source_kind") );

        dbStt.bindString(2, insertParams.get("img_uri"));

        String actionUri = insertParams.get("intent_action_uri");
        if (actionUri == null) {
            dbStt.bindNull(3);
        } else {
            dbStt.bindString(3, actionUri);
        }

        String deviceImgUri = insertParams.get("device_img_uri");
        dbStt.bindString(4, deviceImgUri);

        //// insert実行
        dbStt.executeInsert();
    }

    public void insertAndSaveBitmap(Map<String, String> insertParams, Bitmap bitmap)
            throws Exception {
        insert(insertParams);
        saveImg(bitmap, insertParams.get("device_img_uri"));
    }

    /**
     * idを指定して削除
     * @param id 削除対象のid
     * @return 削除したレコード数
     */
    public int deleteHistories(long id) {
        return mDbWritable.delete(TABLE_NAME, "_id = ?", new String[] {String.valueOf(id)});

    }

    /**
     * 古いものを削除
     * @param maxNum 残しておく最大値
     */
    @Nullable
    public Cursor deleteHistoriesOverflowMax(int maxNum) {
        // ----------------------------------
        // 全体のレコード数を取得
        // ----------------------------------
        Cursor countCursor = mDbWritable.rawQuery(
                "SELECT count(*) AS count FROM histories", null);
        // TODO これじたいいらないんじゃ・・・
        if (countCursor == null) {
            throw new NullPointerException("数が取得できません。");// TODO メッセージをちゃんとする
        }

        if ( !countCursor.moveToFirst() ) {
            countCursor.close();
            throw new NullPointerException("レコード数取得時にcursorから値を取得できませんでした。");
        }
        int allCount = countCursor.getInt(
                countCursor.getColumnIndexOrThrow("count"));
        countCursor.close();

        // ----------------------------------
        // count の判定、最大値超えないときはここで終わり
        // ----------------------------------
        if (allCount <= maxNum) {
            return null;
        }

        // ----------------------------------
        //  DELETE
        // ----------------------------------
        //// 削除情報取得
        Cursor delCursor = mDbReadable.query(TABLE_NAME, PROJECTION, null,
                null,null, null, "created_at ASC",
                String.valueOf(allCount - maxNum));

        //// 情報取得
        int[] historyIds = new int[delCursor.getCount()];
        String[] uris = new String[delCursor.getCount()];
        int i = 0;
        while (delCursor.moveToNext()) {
            historyIds[i] = delCursor.getInt(
                    delCursor.getColumnIndexOrThrow("_id"));
            uris[i] = delCursor.getString(
                    delCursor.getColumnIndexOrThrow("device_img_uri"));
            i++;
        }

        //// 削除
        deleteByIds(historyIds);
        deleteImgs(uris);

        delCursor.moveToFirst();
        return delCursor;
    }

    /**
     * 削除
     * @param historyIds 削除対象の history_id 複数
     * @return 削除数
     */
    public int deleteByIds(int[] historyIds) {
        //// string 型に変換
        String[] historyIdsStr = new String[historyIds.length];
        for (int i=0; i<historyIds.length; i++) {
            historyIdsStr[i] = String.valueOf(historyIds[i]);
        }

        //// プレースホルダの文字列を作成
        String placeholderStr = HistoryModel.makePlaceholderStr(historyIds.length);

        //// 削除
        int deletedNum = mDbWritable.delete(
                TABLE_NAME,
                "_id IN (" + placeholderStr + ")",
                historyIdsStr);

        return deletedNum;
    }

    private static String makePlaceholderStr(int num) {
        if (num <= 0) {
            throw new IllegalArgumentException("numは1以上の数字を指定してください。");
        }
        StringBuilder builder = new StringBuilder();

        builder.append("?");
        for(int i=1; i<num; i++) {
            builder.append(", ?");
        }
        return builder.toString();
    }

    /**
     * 日時を yyyy-mm-dd hh:mm:ss 形式からUnixTimeに変換（UTC）
     * @param yyyymmddhhmmss 変換したい日時
     * @return UnixTime(millisecond)
     * @throws ParseException 変換失敗時例外を投げる
     */
    public static long sqliteToUnixTimeMillis(String yyyymmddhhmmss) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        sdf.setTimeZone( TimeZone.getTimeZone("UTC") );
        Date date = sdf.parse(yyyymmddhhmmss);
        return date.getTime();
    }
}
