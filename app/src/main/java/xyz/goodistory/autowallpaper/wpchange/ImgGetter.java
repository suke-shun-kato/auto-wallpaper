package xyz.goodistory.autowallpaper.wpchange;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.Nullable;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 画像取得インターフェイス
 * Created by k-shunsuke on 2017/12/14.
 */
@SuppressWarnings("WeakerAccess")
public class ImgGetter {
    // --------------------------------------------------------------------
    // フィールド
    // --------------------------------------------------------------------
    /** 画像の自体のURI、Twitterだと「https://.....png」、ディレクトリだと「content://.....」 */
    protected final String imgUri;

    /** 画像が掲載されているページのURL、履歴の画像をクリックしたら飛ぶ場所 */
    protected final String actionUri;

    /** 画像の取得元の種類、HistoryModel.SOURCE_XXXの値 */
    protected final String sourceKind;

    // --------------------------------------------------------------------
    // コンストラクタ
    // --------------------------------------------------------------------
    protected ImgGetter(String imgUri, String actionUri, String sourceKind) {
        this.imgUri = imgUri;
        this.actionUri = actionUri;
        this.sourceKind = sourceKind;
    }
    // --------------------------------------------------------------------
    // メソッド（アクセサ）
    // --------------------------------------------------------------------
    public String getImgUri() {
        return this.imgUri;
    }
    public String getActionUri() {
        return this.actionUri;
    }
    public String getSourceKind() {
        return this.sourceKind;
    }
    // --------------------------------------------------------------------
    // メソッド（通常）
    // --------------------------------------------------------------------
    public Bitmap getImgBitmap(Context context) {
        return getImgBitmapStatic(this.imgUri, context);
    }

    // --------------------------------------------------------------------
    //
    // --------------------------------------------------------------------
    /************************************
     * Bitmapオブジェクトを取得する
     * サブクラスに実装しなかったのは別々のサブクラスでもUriのスキームが同じ場合があるから
     */
    @Nullable
    public static Bitmap getImgBitmapStatic(String imgUri, Context context) {
        // ----------
        // WEB上の画像のとき
        // ----------
        if (imgUri.startsWith("https:") || imgUri.startsWith("http:")) {

            try {
                URL url = new URL(imgUri);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");

                return BitmapFactory.decodeStream(con.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

        // ----------
        //
        // ----------    
        } else if(imgUri.startsWith("file:") ) {    //file:スキームは実際には使われていない、現在はcontent:が使われている
            return BitmapFactory.decodeFile(imgUri.replace("file://", ""));
        } else if(imgUri.startsWith("content:") ) {
            try {
                InputStream is = context.getContentResolver().openInputStream(Uri.parse(imgUri));
                if (is == null) {
                    return null;
                } else {
                    return BitmapFactory.decodeStream(new BufferedInputStream(is));
                }
            } catch (FileNotFoundException e){
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }
}
