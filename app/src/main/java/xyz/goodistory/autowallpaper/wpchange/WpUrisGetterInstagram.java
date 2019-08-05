package xyz.goodistory.autowallpaper.wpchange;

import android.content.Context;
import androidx.annotation.Nullable;

import com.github.scribejava.apis.InstagramApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import xyz.goodistory.autowallpaper.HistoryModel;
import xyz.goodistory.autowallpaper.SettingsPreferenceFragment;

/**
 * Instagram
 */
public class WpUrisGetterInstagram extends WpUrisGetter {
    private final Context mContext;

    WpUrisGetterInstagram(Context context) {
        mContext = context;
    }

    /**
     * APIから帰ってきたJSONから画像URLと掲載ページのURLを抜き出す
     *
     * ・画像
     * data -> images -> standard_resolution -> url
     * data -> carousel_media ->images -> standard_resolution -> url
     *
     * ・ページ
     * data -> link
     *
     * @param jsonObject /users/self/media/recent のapiから返ってきた値
     * @return 下記
     *  [
     *      {
     *          "img": (string、画像のURL),
     *          "action": (string、画像が載っているページのurl)
     *      },
     *      {...},
     *      ...
     *  ]
     */
    private static List<Map<String, String>> pullImgUrls(JSONObject jsonObject) throws JSONException{
        // 戻り値
        List<Map<String, String>> urisList =  new ArrayList<>();

        JSONArray dataJsons = jsonObject.getJSONArray("data");
        for (int i = 0; i < dataJsons.length(); i++) {
            JSONObject itemJson = dataJsons.getJSONObject(i);

            //// 取得画像とページのURLの抜き出し
            String imgUrl = pullImgUrlFromMedia( itemJson );
            String actionUrl = pullActionUrlFromMedia( itemJson );

            if (imgUrl != null) {
                Map<String, String> uris = new HashMap<>();
                uris.put("img", imgUrl);
                uris.put("action", actionUrl);

                urisList.add(uris);
            }

            //// 複数枚のときのみの画像URL抜き出し
            if ( !itemJson.isNull("carousel_media") ) {
                JSONArray data2Jsons = itemJson.getJSONArray("carousel_media");
                for (int j = 0; j < data2Jsons.length(); j++) {
                    // 画像URL抜き出し
                    String imgUrl2 = pullImgUrlFromMedia(data2Jsons.getJSONObject(j));
                    if ( imgUrl2 == null ) {
                        continue;
                    }

                    Map<String, String> uris2 = new HashMap<>();
                    uris2.put("img",imgUrl2);
                    uris2.put("action", actionUrl);
                    urisList.add(uris2);
                }
            }
        }

        //// 重複を取り除く
        List<Map<String, String>> urisListDistinct = new ArrayList<>();
        for (Map<String, String> uris: urisList) {
            String imgUrl = uris.get("img");

            if ( !WpUrisGetterInstagram.existsImgUrl(urisListDistinct, imgUrl) ) {
                urisListDistinct.add(uris);
            }
        }

        return urisListDistinct;
    }

    /**
     * urisListにすでに同じ同じimgUriがあるかどうか
     */
    private static boolean existsImgUrl(List<Map<String, String>> urisList, String imgUrl) {
        for (Map<String, String> uris: urisList) {
            if ( imgUrl.equals(uris.get("img")) ) {
                return true;
            }
        }

        return false;
    }


    /**
     * 画像URLを抜き出す
     * @param mediaJson media
     * @return 画像のURL
     * @throws JSONException JSONのパースが上手くいかなかったときthrows
     */
    @Nullable
    private static String pullImgUrlFromMedia(JSONObject mediaJson) throws JSONException {

        if ( mediaJson.isNull("images") ) {
            return null;
        } else {
            return mediaJson.getJSONObject("images").getJSONObject("standard_resolution").getString("url");
        }
    }

    /**
     * 投稿画像のURLを抜き出す
     * @param mediaJson media
     * @return 投稿画像のurl
     * @throws JSONException JSONのパースが上手くいかなかったときthrows
     */
    private static String pullActionUrlFromMedia(JSONObject mediaJson) throws JSONException {
        if (mediaJson.isNull("link")) {
            return null;
        } else {
            return mediaJson.getString("link");
        }
    }

    /**
     * @param context コンテキスト
     * @return 取得した
     */
    private static JSONObject getSelfMediaJson(Context context)
            throws InterruptedException, ExecutionException, IOException, JSONException {

        //// 変数の準備
        final String clientID = SettingsPreferenceFragment.getInstagramClientID(context);
        final String accessToken = SettingsPreferenceFragment.getInstagramAccessToken(context);
        final String resourceUrl = "https://api.instagram.com/v1/users/self/media/recent";

        //// リクエストの作成
        final OAuth20Service oAuth20Service = new ServiceBuilder(clientID)
                .build(InstagramApi.instance());
        final OAuthRequest request = new OAuthRequest(Verb.GET, resourceUrl);
        oAuth20Service.signRequest(new OAuth2AccessToken(accessToken), request);

        ///// APIにアクセス＆取得
        final Response response = oAuth20Service.execute(request);

        //// レスポンスの処理
        int code = response.getCode();
        if (code < 200 || code >= 300) { // HTTPステータスコードが200番台でないときエラー
            throw new FileNotFoundException("HTTPステータスコードが200番台以外です");
        }

        return new JSONObject(response.getBody());
    }

    public List<ImgGetter> getImgGetterList() throws Exception {

        // APIにアクセスしてJSONを取得
        JSONObject responseJson = getSelfMediaJson(mContext);

        // 画像のURLだけ抜き出す
        List<Map<String, String>> urisList = pullImgUrls(responseJson);

        // imgGetter生成 に変換
        List<ImgGetter> imgGetters = new ArrayList<>();
        for (Map<String, String> uris: urisList) {
            ImgGetter imgGetter = new ImgGetter(uris.get("img"), uris.get("action"), HistoryModel.SOURCE_IS);
            imgGetters.add(imgGetter);
        }

        return imgGetters;
    }
}