package xyz.monogatari.suke.autowallpaper.wpchange;

import android.graphics.Bitmap;

/**
 * 画像取得インターフェイス
 * Created by k-shunsuke on 2017/12/14.
 */
@SuppressWarnings("WeakerAccess")
public interface ImgGetter {
    /************************************
     * 壁紙用の画像データを取得するメソッド
     * @return  取得した画像データ
     */
    @SuppressWarnings("UnnecessaryInterfaceModifier")
    public Bitmap getImg();
}
