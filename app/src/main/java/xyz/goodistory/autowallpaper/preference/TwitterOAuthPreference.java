package xyz.goodistory.autowallpaper.preference;

import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.PatternMatcher;
import android.preference.Preference;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Toast;

import com.github.scribejava.apis.TwitterApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.oauth.OAuth10aService;
import com.github.scribejava.core.oauth.OAuth20Service;

import org.json.JSONException;
import org.json.JSONObject;

import xyz.goodistory.autowallpaper.R;

/**
 * Twitter認証のためのPreference
 * Created by k-shunsuke on 2017/12/23.
 * TODO .preference パッケージに移動
 * TODO Twitter4Jを廃止
 */
public class TwitterOAuthPreference extends Preference {
    // --------------------------------------------------------------------
    // フィールド
    // --------------------------------------------------------------------
    /** 認証用のライブラリ */
    private final OAuth10aService mOAuth10aService;
    /** 認証後のコールバックURL、アクセストークン取得場所 */
    private String mCallbackUrl;

    private final String mTextCantAccessAuthPage;
    /** Toastの文字の設定 TODO finalとか変数名をちゃんとする*/
    private String textOauthSuccess;
    private String textOauthFailed;
    private final String mSummaryDone;
    private final String mSummaryNotYet;


    // --------------------------------------------------------------------
    // 定数
    // --------------------------------------------------------------------
//    public static final String CALLBACK_URL = "xyzgisautowallpaper://";
    // TODO リソースに移動する
    @SuppressWarnings("WeakerAccess")
    public static final String KEY_TOKEN = "token";
    @SuppressWarnings("WeakerAccess")
    public static final String KEY_TOKEN_SECRET = "token_secret";


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
            this.textOauthSuccess
                     = typedAry.getString(R.styleable.TwitterOAuthPreference_textOauthSuccess);
            this.textOauthFailed
                     = typedAry.getString(R.styleable.TwitterOAuthPreference_textOauthFailed);
            mSummaryDone = typedAry.getString(R.styleable.TwitterOAuthPreference_summaryDone);
            mSummaryNotYet = typedAry.getString(R.styleable.TwitterOAuthPreference_summaryNotYet);

            //// 認証に必要な値
            mCallbackUrl= typedAry.getString(R.styleable.TwitterOAuthPreference_callbackUrl);


            //// Oauth10aService
            final String apiKey= typedAry.getString(R.styleable.TwitterOAuthPreference_apiKey);
            final String apiSecretKey
                    = typedAry.getString(R.styleable.TwitterOAuthPreference_apiSecretKey);
            mOAuth10aService = new ServiceBuilder(apiKey)
                    .apiSecret(apiSecretKey)
                    .callback(mCallbackUrl)
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
        private final Context mContext;
        private final String mTextCantGetRequestToken;

        /**
         * @param oAuth10aService
         * @param context
         * @param textCantGetRequestToken
         */
        GetRequestTokenAsyncTask(OAuth10aService oAuth10aService, Context context, String textCantGetRequestToken) {
            super();
            mOAuth10aService = oAuth10aService;
            mContext = context;
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
            if (oAuth1RequestToken == null) {
            // リクエストトークン取得できなかったとき（ネットつながってないとき）
                Toast.makeText(mContext,  mTextCantGetRequestToken, Toast.LENGTH_SHORT)
                        .show();
            } else {
            // リクエストトークン取得できたとき
                //// 認可ページにインテント
                // 認可ページのURLを取得
                String urlAuthorizationPage = mOAuth10aService.getAuthorizationUrl(oAuth1RequestToken);

                // TODO ここでWEBにアクセスできなかったときどうするか？
                // TODO ここ、コールバックURLの設定は？
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlAuthorizationPage));


                mContext.startActivity(intent);
            }

        }

    }

//    /************************************
//     * アクセストークンを取得するクラス
//     */
//    private static class GetAccessTokenAsyncTask extends  AsyncTask<Void, Void, AccessToken> {
//        private final TwitterOAuthPreference twPreference;
//        private final RequestToken requestToken;
//        private final String verifierStr;
//
//        /************************************
//         * コンストラクタ
//         */
//        public GetAccessTokenAsyncTask(
//                TwitterOAuthPreference twPreference, RequestToken requestToken, String verifierStr) {
//
//            super();
//            this.twPreference = twPreference;
//            this.requestToken = requestToken;
//            this.verifierStr = verifierStr;
//        }
//
//        /************************************
//         * verifierとRequestTokenでAccessTokenをTwitterから取得
//         * @param params [0]:verifier
//         * @return AccessTokenオブジェクト、twitter4J
//         */
//        @Override
//        protected AccessToken doInBackground(Void... params) {
//            try {
//                // アクセストークンを取得
//                return this.twPreference.twitter.getOAuthAccessToken(this.requestToken, this.verifierStr);
//            } catch (TwitterException e) {
//                e.printStackTrace();
//                Log.d("○○○",e.getMessage());
//            }
//            return null;
//        }
//
//        /************************************
//         *
//         */
//        @Override
//        protected void onPostExecute(AccessToken accessToken) {
//            if (accessToken != null) {
//                // 認証成功！
//                Toast.makeText(this.twPreference.getContext(),
//                        this.twPreference.textOauthSuccess,
//                        Toast.LENGTH_SHORT
//                ).show();
//
//                //アクセストークンを保存
//                try {
//                    JSONObject aTokenJson = new JSONObject()
//                            .put(KEY_TOKEN, accessToken.getToken())
//                            .put(KEY_TOKEN_SECRET, accessToken.getTokenSecret());
//                    this.twPreference.persistString(aTokenJson.toString());
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//            } else {
//                // 認証失敗。。。
//                Toast.makeText(
//                        this.twPreference.getContext(),
//                        this.twPreference.textOauthFailed,
//                        Toast.LENGTH_SHORT
//                ).show();
//            }
//        }
//    }


    // TODO 認可ページからのコールバックのアクセストークンを取得する
    private class CallbackBroadcastReceiver extends BroadcastReceiver {
        /**
         * @param context The Context in which the receiver is running.
         * @param intent  The Intent being received.
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            intent.getData();

            context.unregisterReceiver(this);
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
        CallbackBroadcastReceiver callbackBroadcastReceiver = new CallbackBroadcastReceiver();

        //// intentFilter の設定
        IntentFilter intentFilter = getCallbackIntentFilter(mCallbackUrl);

        //// 登録
        getContext().registerReceiver(callbackBroadcastReceiver, intentFilter);

        // ----------------------------------
        // 非同期でアクセストークン取得する
        // ----------------------------------
        GetRequestTokenAsyncTask getRequestTokenAsyncTask = new GetRequestTokenAsyncTask(
                mOAuth10aService, getContext(), mTextCantAccessAuthPage);

        // requestTokenを取得 → それで認可ページを開く
        getRequestTokenAsyncTask.execute();
    }

    /**
     * コールバックを受信する用の IntentFilter を取得
     * @param callbackUrl
     * @return
     */
    private static IntentFilter getCallbackIntentFilter(String callbackUrl) {
        IntentFilter intentFilter = new IntentFilter();

        // action
        intentFilter.addAction(Intent.ACTION_VIEW);

        // category
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        intentFilter.addCategory(Intent.CATEGORY_BROWSABLE);

        // data(uri)
        Uri callbackUri = Uri.parse(callbackUrl);
        intentFilter.addDataScheme(callbackUri.getScheme());
        intentFilter.addDataAuthority(callbackUri.getHost(), String.valueOf(callbackUri.getPort()));
        intentFilter.addDataPath(callbackUri.getPath(), PatternMatcher.PATTERN_LITERAL);

        return intentFilter;
    }

//
//    /************************************
//     * getIntent()はこのPreferenceでsetIntent()したものを取り出すのでここでは違う
//     */
//    public void onNewIntent(Intent intent) {
//        // ----------------------------------
//        // 例外処理、途中で切り上げ
//        // ----------------------------------
//        if (intent == null
//                || intent.getData() == null // intend.getData() は Uriオブジェクトが返ってくる
//                || !intent.getData().toString().startsWith(this.mCallbackUrl)
//                || intent.getData().getQueryParameter("oauth_verifier") == null //キャンセルボタンを押したとき
//                ) {
//            return;
//        }
//
//        // ----------------------------------
//        // メイン処理、アクセストークンを取得→SharedPreferenceに保存
//        // ----------------------------------
//        // https://developer.yahoo.co.jp/other/oauth/flow.html
//        new GetAccessTokenAsyncTask(
//                this,
//                this.getRequestTokenAsyncTask.getRequestToken(),
//                intent.getData().getQueryParameter("oauth_verifier")
//        ).execute();
//    }

    /************************************
     * 既にアクセストークンを取得すみか
     * @return true:取得済
     */
    public boolean hasAccessToken() {
        try {
            String accessToken = this.getPersistedString(null);
            if (accessToken != null) {
                JSONObject acTokenJson = new JSONObject(accessToken);
                if (acTokenJson.get(KEY_TOKEN) != null && acTokenJson.get(KEY_TOKEN_SECRET) != null) {
                    return true;
                }
            }
            return false;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

}
