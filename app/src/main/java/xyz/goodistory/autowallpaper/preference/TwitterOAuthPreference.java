package xyz.goodistory.autowallpaper.preference;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.Toast;

import com.github.scribejava.apis.TwitterApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.oauth.OAuth10aService;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

import xyz.goodistory.autowallpaper.R;

/**
 * TODO 認証キャンセルのときの処理をちゃんとする
 * Twitter認証のためのPreference
 */
public class TwitterOAuthPreference extends Preference {
    // --------------------------------------------------------------------
    // フィールド
    // --------------------------------------------------------------------
    /** 認証用のライブラリ */
    private final OAuth10aService mOAuth10aService;
    private OAuth1RequestToken mOAuth1RequestToken;

    /** 認証ボタンを押すページにアクセスできなかったとき */
    private final String mTextCantAccessAuthPage;
    /** ページから戻ってきて access token を取得できなかったとき */
    private final String mTextOauthFailed;
    /** Toastの文字の設定 */
    private final String mTextOauthSuccess;


    // --------------------------------------------------------------------
    // 定数
    // --------------------------------------------------------------------
    /** オリジナルのbroadcast receiver のアクション */
    private static final String ACTION_TO_ACCESS_TOKEN
            = "xyz.goodistory.autowallpaper.ACTION_TO_ACCESS_TOKEN";


    private static final String BUNDLE_KEY_TOKEN = "oauth_token";
    private static final String BUNDLE_KEY_VERIFIER = "oauth_verifier";

    private static final String JSON_KEY_TOKEN = "token";
    private static final String JSON_KEY_TOKEN_SECRET = "token_secret";


    // --------------------------------------------------------------------
    // コンストラクタ
    // --------------------------------------------------------------------
    public TwitterOAuthPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        // ----------------------------------
        // XMLのカスタム属性をフィールドに読み込む
        // ----------------------------------
        TypedArray typedAry = context.obtainStyledAttributes(attrs, R.styleable.TwitterOAuthPreference);

        try {
            //// UIの文章
            mTextCantAccessAuthPage
                     = typedAry.getString(R.styleable.TwitterOAuthPreference_textCantAccessAuthPage);
            mTextOauthSuccess
                     = typedAry.getString(R.styleable.TwitterOAuthPreference_textOauthSuccess);
            mTextOauthFailed
                     = typedAry.getString(R.styleable.TwitterOAuthPreference_textOauthFailed);

            //// Oauth10aService
            final String apiKey= typedAry.getString(R.styleable.TwitterOAuthPreference_apiKey);
            final String apiSecretKey
                    = typedAry.getString(R.styleable.TwitterOAuthPreference_apiSecretKey);
            final String callbackUrl= typedAry.getString(R.styleable.TwitterOAuthPreference_callbackUrl);
            mOAuth10aService = new ServiceBuilder(apiKey)
                    .apiSecret(apiSecretKey)
                    .callback(callbackUrl)
                    .build(TwitterApi.instance());
        } finally {
            typedAry.recycle();
        }
    }

    // --------------------------------------------------------------------
    // 内部クラス
    // --------------------------------------------------------------------
    /************************************
     * リクエストトークンを取得 → 認可ページへ飛ばす
     *
     * 非同期処理中に画面回転などが起こると途中で中断される
     */
    private static class GetRequestTokenAsyncTask extends  AsyncTask<Void, Void, OAuth1RequestToken> {
        private final OAuth10aService mOAuth10aService;
        private final String mTextCantGetRequestToken;
        private final WeakReference<TwitterOAuthPreference> mTwitterOAuthPreferenceWeakReference;

        GetRequestTokenAsyncTask( TwitterOAuthPreference twitterOAuthPreference,
                OAuth10aService oAuth10aService, String textCantGetRequestToken) {

            super();

            mTwitterOAuthPreferenceWeakReference = new WeakReference<>(twitterOAuthPreference);
            mOAuth10aService = oAuth10aService;
            mTextCantGetRequestToken = textCantGetRequestToken;
        }

        /************************************
         * RequestTokenの取得
         * WEBにアクセスして時間がかかるので非同期処理
         * @return リクエストトークン
         */
        @Override
        @Nullable
        protected OAuth1RequestToken doInBackground(Void... params) {
            try {
                return mOAuth10aService.getRequestToken();
            } catch (Exception e) {
                return null;
            }
        }

        /************************************
         * 認可ページを開く
         * 非同期処理の結果処理、
         * @param oAuth1RequestToken リクエストトークン
         */
        @Override
        protected void onPostExecute(@Nullable OAuth1RequestToken oAuth1RequestToken) {
            TwitterOAuthPreference twitterOAuthPreference
                    = mTwitterOAuthPreferenceWeakReference.get();

            // フィールドに保存
            twitterOAuthPreference.setOAuth1RequestToken(oAuth1RequestToken);

            if (oAuth1RequestToken == null) {
            // リクエストトークン取得できなかったとき（ネットつながってないとき）
                Toast.makeText(
                        twitterOAuthPreference.getContext(),
                        mTextCantGetRequestToken, Toast.LENGTH_SHORT)
                        .show();
            } else {
            // リクエストトークン取得できたとき
                //// 認可ページにインテント
                // 認可ページのURLを取得
                String urlAuthorizationPage = mOAuth10aService.getAuthorizationUrl(oAuth1RequestToken);

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlAuthorizationPage));
                twitterOAuthPreference.getContext().startActivity(intent);
            }

        }

    }

    /**
     * AccessTokenを取得 → SharedPreference に保存
     */
    private static class GetAccessTokenAsyncTask extends AsyncTask<Void, Void, OAuth1AccessToken> {
        private final OAuth10aService mOAuth10aService;
        private final OAuth1RequestToken mOAuth1RequestToken;
        private final String mVerifier;
        private final String mTextOauthSuccess;
        private final String mTextOauthFailed;
        private final WeakReference<TwitterOAuthPreference> mTwitterOAuthPreferenceWeakReference;

        GetAccessTokenAsyncTask(
                TwitterOAuthPreference twitterOAuthPreference,
                OAuth10aService oAuth10aService, OAuth1RequestToken oauth1RequestToken,
                String verifier,  String textOauthSuccess, String textOauthFailed) {

            super();

            mOAuth10aService = oAuth10aService;
            mOAuth1RequestToken = oauth1RequestToken;
            mVerifier = verifier;
            mTextOauthSuccess = textOauthSuccess;
            mTextOauthFailed = textOauthFailed;
            mTwitterOAuthPreferenceWeakReference = new WeakReference<>(twitterOAuthPreference);
        }

        @Override
        @Nullable
        protected OAuth1AccessToken doInBackground(Void... params) {
            try {
                return mOAuth10aService.getAccessToken(mOAuth1RequestToken, mVerifier);
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * @param oAuth1AccessToken 取得したAccess token
         */
        @Override
        protected void onPostExecute(@Nullable OAuth1AccessToken oAuth1AccessToken) {
            //// エラー処理
            if (oAuth1AccessToken == null) {
                // アクセストークンが取得できなかったとき
                Toast.makeText( mTwitterOAuthPreferenceWeakReference.get().getContext(),
                        mTextOauthFailed, Toast.LENGTH_SHORT)
                        .show();
                return;
            }

            //// JSON文字列にして保存
            try {
                JSONObject aTokenJson = new JSONObject()
                        .put(JSON_KEY_TOKEN, oAuth1AccessToken.getToken())
                        .put(JSON_KEY_TOKEN_SECRET, oAuth1AccessToken.getTokenSecret());
                mTwitterOAuthPreferenceWeakReference.get().persistString(aTokenJson.toString());
            } catch (JSONException e) {
                Toast.makeText(mTwitterOAuthPreferenceWeakReference.get().getContext(),
                        mTextOauthFailed, Toast.LENGTH_SHORT)
                        .show();
            }

            //// Toastで表示
            Toast.makeText(mTwitterOAuthPreferenceWeakReference.get().getContext(),
                    mTextOauthSuccess, Toast.LENGTH_LONG)
                    .show();
        }
    }

    /**
     * ブロードキャストを受信したら Access Token を取得する
     */
    private static class ToGetAccessTokenBroadcastReceiver extends BroadcastReceiver {

        private final OAuth10aService mOAuth10aService;
        private final String mTextOauthSuccess;
        private final String mTextOauthFailed;
        private final WeakReference<TwitterOAuthPreference> mTwitterOAuthPreferenceWeakReference;


        public ToGetAccessTokenBroadcastReceiver(
                TwitterOAuthPreference twitterOAuthPreference,
                OAuth10aService oAuth10aService,
                String textOauthSuccess, String textOauthFailed ) {

            mTwitterOAuthPreferenceWeakReference = new WeakReference<>(twitterOAuthPreference);
            mOAuth10aService = oAuth10aService;
            mTextOauthSuccess = textOauthSuccess;
            mTextOauthFailed = textOauthFailed;
        }

        /**
         * verifier(intentから取得)で access token を取得する
         * @param context The Context in which the receiver is running.
         * @param intent  The Intent being received.
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            //// Receiver を解除
            context.unregisterReceiver(this);

            //// request token と verifier を取得
            String oauthToken = intent.getStringExtra(BUNDLE_KEY_TOKEN);
            String oauthVerifier = intent.getStringExtra(BUNDLE_KEY_VERIFIER);

            // エラー処理
            if (oauthToken.equals("") || oauthVerifier.equals("")) {
                Toast.makeText(context, mTextOauthFailed, Toast.LENGTH_LONG)
                        .show();
                return;
            }

            //// 非同期でaccess token を取得
            TwitterOAuthPreference twitterOAuthPreference = mTwitterOAuthPreferenceWeakReference.get();
            ( new GetAccessTokenAsyncTask(twitterOAuthPreference, mOAuth10aService,
                    twitterOAuthPreference.getOAuth1RequestToken(), oauthVerifier,
                    mTextOauthSuccess, mTextOauthFailed) )
                    .execute();
        }
    }

    /**
     * SharedPreference から Access token と token secret を取得するクラス
     */
    public static class SharedPreference {
        private JSONObject mTokensJson;


        /**
         * アクセストークンを取得
         * @param preferenceKey preferenceのkey
         * @param context context
         */
        public SharedPreference(String preferenceKey, Context context) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            try {
                mTokensJson = new JSONObject( sp.getString(preferenceKey, null) );
            } catch (JSONException e) {
                mTokensJson = new JSONObject();
            }
        }

        /**
         * Preferenceに保存したAccessTokenを取得
         * @return AccessToken
         */
        public String getToken() {
            try {
                return mTokensJson.getString(TwitterOAuthPreference.JSON_KEY_TOKEN);
            } catch (JSONException e) {
                return "";
            }
        }

        /**
         * Preferenceに保存したAccessTokenSecretを取得
         * @return AccessTokenSecret
         */
        public String getTokenSecret() {
            try {
                return mTokensJson.getString(TwitterOAuthPreference.JSON_KEY_TOKEN_SECRET);
            } catch (JSONException e) {
                return "";
            }
        }
    }


    // --------------------------------------------------------------------
    // メソッド
    // --------------------------------------------------------------------
    /************************************
     * クリックしたらTwitterの認証ページをWEBプラウザで開く
     * （アプリ内のWebViewで開くことも考えたけど、
     * WEBプラウザなどではクッキーが使えるので敢えてWEBプラウザにした）
     */
    @Override
    protected void onClick() {
        // ----------------------------------
        // BroadcastReceiver をセット
        // ----------------------------------
        //// BroadcastReceiver
        ToGetAccessTokenBroadcastReceiver broadcastReceiver
                = new ToGetAccessTokenBroadcastReceiver(this, mOAuth10aService,
                mTextOauthSuccess, mTextOauthFailed);

        //// intentFilter の設定
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_TO_ACCESS_TOKEN);

        //// 登録
        getContext().registerReceiver(broadcastReceiver, intentFilter);

        // ----------------------------------
        // 非同期でアクセストークン取得する
        // ----------------------------------
        GetRequestTokenAsyncTask getRequestTokenAsyncTask = new GetRequestTokenAsyncTask(
                this, mOAuth10aService, mTextCantAccessAuthPage);

        // requestTokenを取得 → それで認可ページを開く
        getRequestTokenAsyncTask.execute();
    }

    /**
     * AsyncTask内で使う用
     * @return mOAuth1RequestToken
     */
    private OAuth1RequestToken getOAuth1RequestToken() {
        return mOAuth1RequestToken;
    }

    /**
     * * AsyncTask内で使う用
     * @param oAuth1RequestToken oAuth1RequestToken
     */
    private void setOAuth1RequestToken(OAuth1RequestToken oAuth1RequestToken) {
        mOAuth1RequestToken = oAuth1RequestToken;
    }


    /************************************
     * 既にアクセストークンを取得すみか
     * @return true:取得済
     */
    public boolean hasAccessToken() {
        //// 値を保存していないとき
        String accessTokenJsonStr = getPersistedString(null);
        if (accessTokenJsonStr == null) {
            return false;
        }

        //// 値を保存しているとき
        try {
            JSONObject accessTokenJson = new JSONObject(accessTokenJsonStr);
            if ( accessTokenJson.get(JSON_KEY_TOKEN) == null
                    || accessTokenJson.get(JSON_KEY_TOKEN_SECRET) == null) {
                throw new JSONException("保存したJSONの値がおかしいです。");
            }

            return true;
        } catch (JSONException e) {
            return false;
        }
    }

    // --------------------------------------------------------------------
    // メソッド, static
    // --------------------------------------------------------------------
    /**
     * Activity で使うメソッド
     * 認証ボタンを押したあとのcallbackで取得したintent(verifierとrequestToken)を
     * このクラスへbroadcastでを送る
     * @param intent callbackで取得したintent
     * @param context context
     */
    public static void sendToAccessTokenBroadcast(Intent intent, Context context) {
        //// 引数を処理
        String oauthToken;
        String oauthVerifier;
        Uri uri = intent.getData();

        if (uri == null) {
            oauthToken = "";
            oauthVerifier = "";
        } else {
            oauthToken = uri.getQueryParameter("oauth_token");
            oauthVerifier = uri.getQueryParameter("oauth_verifier");
        }

        //// broadcast を送信
        Intent sendIntent = new Intent();
        sendIntent.setAction(ACTION_TO_ACCESS_TOKEN);
        sendIntent.putExtra(BUNDLE_KEY_TOKEN, oauthToken);
        sendIntent.putExtra(BUNDLE_KEY_VERIFIER, oauthVerifier);
        context.sendBroadcast(sendIntent);
    }

}
