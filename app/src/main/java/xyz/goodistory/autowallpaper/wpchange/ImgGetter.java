package xyz.goodistory.autowallpaper.wpchange;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.ArrayMap;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
    protected final String mImgUri;
    @Nullable
    protected String mDeviceImgUri = null;

    /** 画像が掲載されているページのURL、履歴の画像をクリックしたら飛ぶ場所 */
    @Nullable
    protected final String mActionUri;

    /** 画像の取得元の種類、HistoryModel.SOURCE_XXXの値 */
    protected final String mSourceKind;

    // --------------------------------------------------------------------
    // コンストラクタ
    // --------------------------------------------------------------------
    protected ImgGetter(String imgUri, @Nullable String actionUri, String sourceKind) {
        this.mImgUri = imgUri;
        this.mActionUri = actionUri;
        this.mSourceKind = sourceKind;
    }

    protected ImgGetter(String imgUri, @Nullable String actionUri, String sourceKind, @Nullable String deviceImgUri) {
        this.mImgUri = imgUri;
        this.mActionUri = actionUri;
        this.mSourceKind = sourceKind;
        this.mDeviceImgUri = deviceImgUri;
    }

    // --------------------------------------------------------------------
    // メソッド（アクセサ）
    // --------------------------------------------------------------------
    public String getSourceKind() {
        return mSourceKind;
    }
    public String getImgUri() {
        return mImgUri;
    }
    public String getActionUri() {
        return mActionUri;
    }
    public String getDeviceImgUri() {
        return mDeviceImgUri;
    }
    public Map<String, String> getAll() {
        Map<String, String> fMap = new HashMap<>();
        fMap.put("source_kind", getSourceKind());
        fMap.put("img_uri", getImgUri());
        fMap.put("intent_action_uri", getActionUri());
        fMap.put("device_img_uri", getDeviceImgUri());
        return fMap;
    }

    public String generateDeviceImgName() {
        Long nowMillis = System.currentTimeMillis();

        return nowMillis.toString() + Uri.encode(this.getImgUri()) + ".png";
    }
    // --------------------------------------------------------------------
    // メソッド（通常）
    // --------------------------------------------------------------------

    /**
     * this.mImgUri から Bitmapオブジェクトを取得する
     * @param context コンテクスト
     * @return 取得したBitmap
     */
    @Nullable
    public Bitmap getImgBitmap(Context context) {
        // ----------
        // WEB上の画像のとき
        // ----------
        if (mImgUri.startsWith("https:") || mImgUri.startsWith("http:")) {

            try {
                URL url = new URL(mImgUri);
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
        } else if(mImgUri.startsWith("file:") ) {    //file:スキームは実際には使われていない、現在はcontent:が使われている
            return BitmapFactory.decodeFile(mImgUri.replace("file://", ""));
        } else if(mImgUri.startsWith("content:") ) {
            try {
                InputStream is = context.getContentResolver().openInputStream(Uri.parse(mImgUri));
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
