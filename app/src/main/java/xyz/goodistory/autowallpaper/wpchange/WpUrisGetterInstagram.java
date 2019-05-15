package xyz.goodistory.autowallpaper.wpchange;

import android.content.Context;
import android.support.annotation.Nullable;

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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import xyz.goodistory.autowallpaper.HistoryModel;
import xyz.goodistory.autowallpaper.SettingsFragment;

/**
 * Instagram
 */
public class WpUrisGetterInstagram extends WpUrisGetter {
    private final Context mContext;

    WpUrisGetterInstagram(Context context) {
        mContext = context;
    }

    /**
     * APIから帰ってきたJSONから画像URL（複数）のみ抜き出す
     * data -> images -> standard_resolution -> url
     * data -> carousel_media ->images -> standard_resolution -> url
     */
    private static Set<String> pullImgUrls(JSONObject jsonObject) throws JSONException{
        Set<String> imgUrls = new HashSet<>();
        JSONArray dataJsons = jsonObject.getJSONArray("data");

        for (int i = 0; i < dataJsons.length(); i++) {
            JSONObject itemJson = dataJsons.getJSONObject(i);

            //// 通常の画像URL抜き出し
            String imgUrl = pullImgUrlFromMedia( itemJson );
            if (imgUrl != null) {
                imgUrls.add(imgUrl);
            }

            //// 複数枚のときのみの画像URL抜き出し
            if ( !itemJson.isNull("carousel_media") ) {
                JSONArray data2Jsons = itemJson.getJSONArray("carousel_media");
                for (int j = 0; j < data2Jsons.length(); j++) {
                    // 画像URL抜き出し
                    String imgUrl2 = pullImgUrlFromMedia(data2Jsons.getJSONObject(j));
                    if (imgUrl2 != null) {
                        imgUrls.add(imgUrl2);
                    }
                }
            }
        }

        return imgUrls;
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
     * @param context コンテキスト
     * @return 取得した
     */
    private static JSONObject getSelfMediaJson(Context context)
            throws InterruptedException, ExecutionException, IOException, JSONException {

        //// 変数の準備
        final String clientID = SettingsFragment.getInstagramClientID(context);
        final String accessToken = SettingsFragment.getInstagramAccessToken(context);
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

        // 画像のURLだけだけ抜き出す
        Set<String> imgUrls = pullImgUrls(responseJson);

        // imgGetter生成 に変換
        List<ImgGetter> imgGetters = new ArrayList<>();
        for (String imgUrl: imgUrls) {
            // TODO actionUri をちゃんとする
            ImgGetter imgGetter = new ImgGetter(imgUrl, null, HistoryModel.SOURCE_IS);
            imgGetters.add(imgGetter);
        }

        return imgGetters;
    }
}