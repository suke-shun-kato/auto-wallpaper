package xyz.goodistory.autowallpaper.wpchange;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import xyz.goodistory.autowallpaper.HistoryModel;
import xyz.goodistory.autowallpaper.R;
//import xyz.goodistory.autowallpaper.preference.SelectDirectoryPreferenceOld;
import xyz.goodistory.autowallpaper.util.FileExtended;


/**
 * 指定ディレクトリからランダムに画像データを取得するクラス
 * Created by k-shunsuke on 2017/12/14.
 */
class WpUrisGetterDirectory extends WpUrisGetter {
    // --------------------------------------------------------------------
    // フィールド
    // --------------------------------------------------------------------
    private static final String[] EXTENSIONS = {"jpg", "jpeg", "png"};

    private final Context mContext;

    // --------------------------------------------------------------------
    // コンストラクタ
    // --------------------------------------------------------------------
    WpUrisGetterDirectory(Context context) {
        mContext = context;
    }

    // --------------------------------------------------------------------
    // メソッド
    // --------------------------------------------------------------------
    /************************************
     * 画像一覧から画像のURIを抽選する
     * @return boolean true:成功したとき、false:失敗したとき（ファイルが0のときなど）
     */
    public List<ImgGetter> getImgGetterList() {
        List<ImgGetter> getImgGetterList = new ArrayList<>();

        // ----------------------------------
        // 取得対象の画像のパスリストを取得
        // ----------------------------------
        //// 例外処理、ストレージアクセスパーミッションがなければ途中で切り上げ
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            return getImgGetterList;
        }

        //// 通常処理
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        String keySelectDirectory = mContext.getString(R.string.preference_key_select_directory);
        FileExtended imgDirFileEx = new FileExtended(
                sp.getString(keySelectDirectory,
                        "" )
//                        SelectDirectoryPreferenceOld.DEFAULT_DIR_PATH_WHEN_NO_DEFAULT ) // TODO 戻す
        );
        List<String> imgPathList = imgDirFileEx.getAllFilePathList(EXTENSIONS);

        // ----------------------------------
        //
        // ----------------------------------
        for (String imgPath : imgPathList) {
            //// ここで「file://」→「content://」へ変換する
            Uri contentUri = FileProvider.getUriForFile(mContext,
                    mContext.getPackageName() + ".fileprovider", new File(imgPath));

            //// Listに追加
            getImgGetterList.add( new ImgGetter(
                    contentUri.toString(),
                    contentUri.toString(),
                    HistoryModel.SOURCE_DIR) );
        }

        return getImgGetterList;
    }
}
