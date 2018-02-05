package xyz.monogatari.suke.autowallpaper.wpchange;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
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
    final Context context;
    String imgUri = null;
    String actionUri = null;

    // --------------------------------------------------------------------
    // コンストラクタ
    // --------------------------------------------------------------------
    @SuppressWarnings("WeakerAccess")
    public ImgGetter(Context context) {
        this.context = context;
    }
    // --------------------------------------------------------------------
    // 抽象メソッド
    // --------------------------------------------------------------------
    public abstract boolean drawImg();

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
    public Bitmap getImgBitmap() {
        return getImgBitmapStatic(this.imgUri);
    }

    // --------------------------------------------------------------------
    //
    // --------------------------------------------------------------------
    @Nullable
    public static Bitmap getImgBitmapStatic(String imgUri) {
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
        } else if(imgUri.startsWith("file:") ) {
            return BitmapFactory.decodeFile(imgUri.replace("file://", ""));
        } else {
            return null;
        }
    }
}
