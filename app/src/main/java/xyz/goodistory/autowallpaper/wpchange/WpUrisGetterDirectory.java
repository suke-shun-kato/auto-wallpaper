package xyz.goodistory.autowallpaper.wpchange;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import xyz.goodistory.autowallpaper.HistoryModel;
import xyz.goodistory.autowallpaper.R;
import xyz.goodistory.autowallpaper.preference.SelectImageBucketPreference;


/**
 * 指定ディレクトリからランダムに画像データを取得するクラス
 * Created by k-shunsuke on 2017/12/14.
 */
class WpUrisGetterDirectory extends WpUrisGetter {
    // --------------------------------------------------------------------
    // フィールド
    // --------------------------------------------------------------------
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
        // 例外処理、ストレージアクセスパーミッションがなければ途中で切り上げ
        // ----------------------------------
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            return getImgGetterList;
        }

        // ----------------------------------
        // 取得対象の画像のパスリストを取得
        // ----------------------------------
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        final String keySelectDirectory = mContext.getString(R.string.preference_key_select_image_bucket);
        final List<Uri> imageUris
                = SelectImageBucketPreference.getUrisFromSharedPreferences(
                        sp, keySelectDirectory, mContext.getContentResolver() );

        for (Uri uri: imageUris) {
            final ImgGetter imgGetter = new ImgGetter(uri.toString(), uri.toString(), HistoryModel.SOURCE_DIR);
            getImgGetterList.add(imgGetter);
        }

        return getImgGetterList;
    }
}
