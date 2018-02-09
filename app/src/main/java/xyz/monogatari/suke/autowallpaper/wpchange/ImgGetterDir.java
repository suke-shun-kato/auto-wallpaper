package xyz.monogatari.suke.autowallpaper.wpchange;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import xyz.monogatari.suke.autowallpaper.SelectDirPreference;
import xyz.monogatari.suke.autowallpaper.SettingsFragment;
import xyz.monogatari.suke.autowallpaper.util.FileExtended;


/**
 * 指定ディレクトリからランダムに画像データを取得するクラス
 * Created by k-shunsuke on 2017/12/14.
 */

@SuppressWarnings("WeakerAccess")
public class ImgGetterDir extends ImgGetter {
    // --------------------------------------------------------------------
    // フィールド
    // --------------------------------------------------------------------
    private static final String[] EXTENSION_ARY = {"jpg", "jpeg", "png"};

    // --------------------------------------------------------------------
    // コンストラクタ
    // --------------------------------------------------------------------
    @SuppressWarnings("WeakerAccess")
    public ImgGetterDir(String imgUri, @SuppressWarnings("SameParameterValue") String actionUri) {
        super(imgUri, actionUri);
    }

    // --------------------------------------------------------------------
    // メソッド
    // --------------------------------------------------------------------
//    /************************************
//     * 画像一覧から画像のURIを抽選する
//     * @return boolean true:成功したとき、false:失敗したとき（ファイルが0のときなど）
//     */
//    public boolean drawImg() {
//        // ----------------------------------
//        // 取得対象の画像のパスリストを取得
//        // ----------------------------------
//        //// 例外処理、ストレージアクセスパーミッションがなければ途中で切り上げ
//        if (ContextCompat.checkSelfPermission(this.context, Manifest.permission.READ_EXTERNAL_STORAGE)
//                != PackageManager.PERMISSION_GRANTED) {
//Log.d("○" + this.getClass().getSimpleName(), "ストレージアクセス権限がない！！！");
//            return false;
//        }
//
//        //// 通常処理
//        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.context);
//        FileExtended imgDirFileEx = new FileExtended(
//                sp.getString(
//                        SettingsFragment.KEY_FROM_DIR_PATH,
//                        SelectDirPreference.DEFAULT_DIR_PATH_WHEN_NO_DEFAULT
//                )
//        );
//        List<String> imgPathList = imgDirFileEx.getAllFilePathList(EXTENSION_ARY);
//
//        // ----------------------------------
//        // 抽選
//        // ----------------------------------
//        if (imgPathList.size() == 0) {
//            return false;
//        }
//
//        int drawnIndex = new Random().nextInt(imgPathList.size());
//        this.imgUri = "file://" + imgPathList.get(drawnIndex);
//        this.actionUri = null;
//
//        return true;
//    }
    /************************************
     * 画像一覧から画像のURIを抽選する
     * @return boolean true:成功したとき、false:失敗したとき（ファイルが0のときなど）
     */
    public static List<ImgGetterDir> getImgGetterList(Context context) {
        List<ImgGetterDir> getImgGetterList = new ArrayList<>();



        // ----------------------------------
        // 取得対象の画像のパスリストを取得
        // ----------------------------------
        //// 例外処理、ストレージアクセスパーミッションがなければ途中で切り上げ
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
Log.d("○ImgGetterDir", "ストレージアクセス権限がない！！！");
            return getImgGetterList;
        }

        //// 通常処理
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        FileExtended imgDirFileEx = new FileExtended(
                sp.getString(
                        SettingsFragment.KEY_FROM_DIR_PATH,
                        SelectDirPreference.DEFAULT_DIR_PATH_WHEN_NO_DEFAULT
                )
        );
        List<String> imgPathList = imgDirFileEx.getAllFilePathList(EXTENSION_ARY);

        // ----------------------------------
        //
        // ----------------------------------
        for (String imgPath : imgPathList) {
            //// ここで「file://」→「content://」へ変換する
            Uri contentUri = FileProvider.getUriForFile(context, "xyz.monogatari.suke.autowallpaper.fileprovider", new File(imgPath));

            //// Listに追加
            getImgGetterList.add(
                    new ImgGetterDir(
                            contentUri.toString(),
                            null
                    )
            );
        }

        return getImgGetterList;
    }



}
