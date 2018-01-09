package xyz.monogatari.suke.autowallpaper.service;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.github.scribejava.apis.TwitterApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth10aService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import xyz.monogatari.suke.autowallpaper.util.Token;

/**
 * Twitterのお気に入りからランダムに画像を取得するクラス
 * Created by k-shunsuke on 2017/12/27.
 */

public class ImgGetterTw implements ImgGetter {
    private final Context context;

    public ImgGetterTw(Context context) {
        this.context = context;
    }

    /************************************
     * APIでTwitterのお気に入りのJSONを取得
     * @return 取得したリストのJSONArray
     */
    private JSONArray getFavList() {
        try {
            String apiUrl =  "https://api.twitter.com/1.1/favorites/list.json?count=200";
            final OAuth10aService service
                    = new ServiceBuilder(Token.getTwitterConsumerKey(this.context))
                         .apiSecret(Token.getTwitterConsumerSecret(this.context))
                         .build(TwitterApi.instance());

            final OAuthRequest request = new OAuthRequest(Verb.GET, apiUrl);
            service.signRequest(
                    new OAuth1AccessToken(
                            Token.getTwitterAccessToken(this.context),
                            Token.getTwitterAccessTokenSecret(this.context)
                    ),
                    request
            );
            final Response response = service.execute(request);
            String responseStr = response.getBody();
Log.d("○△", ""+responseStr.length());
            return new JSONArray(responseStr);

        } catch (IOException e) {
            Log.d("○△",e.getMessage());
            e.printStackTrace();
            return null;
        } catch (InterruptedException e) {
            Log.d("○△",e.getMessage());
            e.printStackTrace();
            return null;
        } catch (ExecutionException e) {
            Log.d("○△",e.getMessage());
            e.printStackTrace();
            return null;
        } catch (JSONException e) {
            Log.d("○△",e.getMessage());
            e.printStackTrace();
            return null;
        }
    }


    /************************************
     * rootJsonAry[].keyName.media[] (JS表記) の部分のJSONを取得する
     * @param rootJsonAry お気に入りリストJSONのルート、配列JSON
     * @param keyName "entities" or "extended_entities"
     * @return 取得したmediaの部分のJSON（複数）
     */
    private static List<JSONObject> getMediaJson(JSONArray rootJsonAry, String keyName) {
        List<JSONObject> list = new ArrayList<>();

        // ----------------------------------
        // 例外処理
        // ----------------------------------
        if (rootJsonAry == null) {
            return list;
        }

        // ----------------------------------
        // メイン処理
        // ----------------------------------
        for (int i=0; i<rootJsonAry.length(); i++) {
            JSONObject json2 = rootJsonAry.optJSONObject(i);
            if ( json2 == null ) {
                continue;
            }

            JSONObject json3 = json2.optJSONObject(keyName);
            if (json3 == null) {
                continue;
            }

            JSONArray json4 = json3.optJSONArray("media");
            if (json4 == null) {
                continue;
            }

            for (int j = 0; j < json4.length(); j++) {
                JSONObject json5 = json4.optJSONObject(j);
                if (json5 == null) {
                    continue;
                }
                list.add(json5);
            }
        }
        return list;
    }

    /************************************
     * Twitterから取得したお気に入りのJSONを扱いやすいように加工する
     * @param jsonAry Twitterから取得したお気に入りリスト
     * @return 加工されたjsonのリスト、下記のjsonのリスト
     * {
     *     media_url_https: (画像のURL),
     *     url: (画像が掲載されているWEBページ),
     *     ....その他 entities 下にある要素 ....
     * }
     */
    private static List<JSONObject> editJson(JSONArray jsonAry) {
Log.d("○", ""+jsonAry);
        List<JSONObject> jsonObj = new ArrayList<>();
        jsonObj.addAll( getMediaJson(jsonAry, "entities") );  //1枚目のメディア画像
        jsonObj.addAll( getMediaJson(jsonAry, "extended_entities") ); //2～4枚目のメディア画像

        return jsonObj;
    }
    
    /************************************
     *
     */
    public Bitmap getImg() {
        // ----------------------------------
        // お気に入りから画像のURLを取得
        // ----------------------------------
        JSONArray favListJsonAry = this.getFavList();

        List<JSONObject> flattenJson = editJson(favListJsonAry);

        // ----------------------------------
        // 抽選
        // ----------------------------------
        if ( flattenJson.size() == 0) {
            return null;
        }
        int drawnIndex = new Random().nextInt(flattenJson.size());
        String imgUrl = flattenJson.get(drawnIndex).optString("media_url_https");

        // ----------------------------------
        // 画像を取得
        // ----------------------------------
        try {
Log.d("○△△△△△△", imgUrl);
            URL url = new URL(imgUrl);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("GET");

            return BitmapFactory.decodeStream(con.getInputStream());
        } catch (IOException e ){
            e.printStackTrace();
            return null;
        }
    }
}