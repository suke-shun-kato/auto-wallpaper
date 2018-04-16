package xyz.monogatari.autowallpaper.wpchange;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

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
public abstract class ImgGetter {
    // --------------------------------------------------------------------
    // フィールド
    // --------------------------------------------------------------------
    /** 画像の自体のURI、Twitterだと「https://.....png」、ディレクトリだと「content://.....」 */
    @SuppressWarnings("CanBeFinal")
    String imgUri;
    /** 画像が掲載されているページのURL、履歴の画像をクリックしたら飛ぶ場所、ディレクトリは「null」 */
    @SuppressWarnings("CanBeFinal")
    String actionUri;

    // --------------------------------------------------------------------
    // コンストラクタ
    // --------------------------------------------------------------------
    protected ImgGetter(String imgUri, String actionUri) {
        this.imgUri = imgUri;
        this.actionUri = actionUri;
    }
    // --------------------------------------------------------------------
    // 抽象メソッド
    // --------------------------------------------------------------------
//    public abstract List<ImgGetter> getImgGetterList();

    // --------------------------------------------------------------------
    // メソッド（アクセサ）
    // --------------------------------------------------------------------
    public String getImgUri() {
        return this.imgUri;
    }
    public String getActionUri() {
        return this.actionUri;
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
                Log.d("○ImgGetter", "壁紙のURI:" + imgUri);
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
