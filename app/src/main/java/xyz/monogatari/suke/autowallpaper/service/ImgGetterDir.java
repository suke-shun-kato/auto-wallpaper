package xyz.monogatari.suke.autowallpaper.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;

import java.util.List;
import java.util.Random;

import xyz.monogatari.suke.autowallpaper.SelectDirPreference;
import xyz.monogatari.suke.autowallpaper.SettingsFragment;
import xyz.monogatari.suke.autowallpaper.util.FileExtended;


/**
 * 指定ディレクトリからランダムに画像データを取得するクラス
 * Created by k-shunsuke on 2017/12/14.
 */

public class ImgGetterDir implements ImgGetter {
    // --------------------------------------------------------------------
    // フィールド
    // --------------------------------------------------------------------
    private final Context context;
    private static final String[] EXTENSION_ARY = {"jpg", "jpeg", "png"};

    // --------------------------------------------------------------------
    // コンストラクタ
    // --------------------------------------------------------------------
    public ImgGetterDir(Context context) {
        this.context = context;
    }

    // --------------------------------------------------------------------
    // メソッド
    // --------------------------------------------------------------------
    /************************************
     *
     * @return 画像データ
     */
    public Bitmap getImg() {
        // ----------------------------------
        // 取得対象の画像のパスリストを取得
        // ----------------------------------
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.context);
        FileExtended imgDirFileEx = new FileExtended(
                sp.getString(
                        SettingsFragment.KEY_FROM_DIR_PATH,
                        SelectDirPreference.DEFAULT_DIR_PATH_WHEN_NO_DEFAULT
                )
        );
        List<String> imgPathList = imgDirFileEx.getAllFilePathList(EXTENSION_ARY);

        // ----------------------------------
        // 抽選
        // ----------------------------------
        int drawnIndex = new Random().nextInt(imgPathList.size());

        // ----------------------------------
        // Bitmap オブジェクトを返す
        // ----------------------------------
        return BitmapFactory.decodeFile(imgPathList.get(drawnIndex));
    }
}
