package xyz.monogatari.autowallpaper;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;


import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

/**
 * Twitter認証のためのPreference
 * Created by k-shunsuke on 2017/12/23.
 */

@SuppressWarnings("ALL")
public class TwitterOAuthPreference extends Preference {
    // --------------------------------------------------------------------
    // フィールド
    // --------------------------------------------------------------------
    /** Twitterオブジェクト、twitter4J */
    private Twitter twitter;
    private GetRequestTokenAsyncTask getRequestTokenAsyncTask;

    /** Toastの文字の設定 */
    private String textCantAccessAuthPage;
    private String textOauthSuccess;
    private String textOauthFailed;

    // --------------------------------------------------------------------
    // 定数
    // --------------------------------------------------------------------
    /** 認証後のコールバックURL、アクセストークン取得場所 */
    public static final String CALLBACK_URL = "xyzgisautowallpaper://";

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
             this.textCantAccessAuthPage
                     = typedAry.getString(R.styleable.TwitterOAuthPreference_textCantAccessAuthPage);
             this.textOauthSuccess
                     = typedAry.getString(R.styleable.TwitterOAuthPreference_textOauthSuccess);
             this.textOauthFailed
                     = typedAry.getString(R.styleable.TwitterOAuthPreference_textOauthFailed);
        } finally {
            typedAry.recycle();
        }
    }


    // --------------------------------------------------------------------
    // メソッド
    // --------------------------------------------------------------------
    /************************************
     * TwitterのAPIサーバーからリクエストトークンを取得するための非同期処理クラス
     * AsyncTaskLoaderを使うことも検討したが敢えて使っていない
     * （ローダーは一度読み込まれると再度読み込む動作には向いてないため）
     * 非同期処理中に画面回転などが起こると途中で中断される
     */
    private static class GetRequestTokenAsyncTask extends  AsyncTask<Void, Void, RequestToken> {
        private final TwitterOAuthPreference twPreference;
        private RequestToken requestToken;

        /************************************
         * コンストラクタ
         */
        GetRequestTokenAsyncTask(TwitterOAuthPreference twPreference) {
            super();
            this.twPreference = twPreference;
        }

        /************************************
         * RequestTokenの取得→それを利用して認証画面のURLを生成
         * WEBにアクセスして時間がかかるので非同期処理
         * @return Twitterの認証画面のURL
         */
        @Override
        protected RequestToken doInBackground(Void... params) {
            try {
                RequestToken rqToken = this.twPreference.twitter.getOAuthRequestToken(CALLBACK_URL);
                return rqToken;
            } catch (Exception e) {
                e.printStackTrace();
               return null;
            }
        }

        /************************************
         * 非同期処理の結果処理、認証のWEBページを開く
         * @param requestToken リクエストトークン
         */
        @Override
        protected void onPostExecute(RequestToken requestToken) {
            this.requestToken = requestToken;

            String url = this.requestToken != null ? this.requestToken.getAuthorizationURL() : null;
            if (url != null) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

                twPreference.getContext().startActivity(intent);
            } else {    //ネットつながってないときなど
                Toast.makeText(
                        this.twPreference.getContext(),
                        this.twPreference.textCantAccessAuthPage,
                        Toast.LENGTH_SHORT
                ).show();
            }
        }

        public RequestToken getRequestToken() {
            return this.requestToken;
        }
    }


    /************************************
     * クリックしたらTwitterの認証ページをWEBプラウザで開く
     * （アプリ内のWebViewで開くことも考えたけど、
     * WEBプラウザなどではクッキーが使えるので敢えてWEBプラウザにした）
     */
    @Override
    protected void onClick() {
        // ----------------------------------
        // Twitterクラスを作成
        // onCreateなどでTwitterオブジェクトを生成するのはNG（2回目以降アクセストークンの取得でエラーが出るため）
        // ----------------------------------
        this.twitter = new TwitterFactory().getInstance();

        //// コンシューマーキー、コンシューマーシークレットのセット
        this.twitter.setOAuthConsumer(
                this.getContext().getString(R.string.twitter_consumer_key),
                this.getContext().getString(R.string.twitter_consumer_secret)
        );

        //// 非同期でアクセストークン取得する
        this.getRequestTokenAsyncTask = new GetRequestTokenAsyncTask(this);
        getRequestTokenAsyncTask.execute();
    }


    /************************************
     * アクセストークンを取得するクラス
     */
    private static class GetAccessTokenAsyncTask extends  AsyncTask<Void, Void, AccessToken> {
        private final TwitterOAuthPreference twPreference;
        private final RequestToken requestToken;
        private final String verifierStr;

        /************************************
         * コンストラクタ
         */
        public GetAccessTokenAsyncTask(
                TwitterOAuthPreference twPreference, RequestToken requestToken, String verifierStr) {

            super();
            this.twPreference = twPreference;
            this.requestToken = requestToken;
            this.verifierStr = verifierStr;
        }

        /************************************
         * verifierとRequestTokenでAccessTokenをTwitterから取得
         * @param params [0]:verifier
         * @return AccessTokenオブジェクト、twitter4J
         */
        @Override
        protected AccessToken doInBackground(Void... params) {
            try {
                // アクセストークンを取得
                return this.twPreference.twitter.getOAuthAccessToken(this.requestToken, this.verifierStr);
            } catch (TwitterException e) {
                e.printStackTrace();
                Log.d("○○○",e.getMessage());
            }
            return null;
        }

        /************************************
         *
         */
        @Override
        protected void onPostExecute(AccessToken accessToken) {
            if (accessToken != null) {
                // 認証成功！
                Toast.makeText(this.twPreference.getContext(),
                        this.twPreference.textOauthSuccess,
                        Toast.LENGTH_SHORT
                ).show();

                //アクセストークンを保存
                try {
                    JSONObject aTokenJson = new JSONObject()
                            .put(KEY_TOKEN, accessToken.getToken())
                            .put(KEY_TOKEN_SECRET, accessToken.getTokenSecret());
                    this.twPreference.persistString(aTokenJson.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else {
                // 認証失敗。。。
                Toast.makeText(
                        this.twPreference.getContext(),
                        this.twPreference.textOauthFailed,
                        Toast.LENGTH_SHORT
                ).show();
            }
        }
    }

    /************************************
     * getIntent()はこのPreferenceでsetIntent()したものを取り出すのでここでは違う
     */
    public void onNewIntent(Intent intent) {
        // ----------------------------------
        // 例外処理、途中で切り上げ
        // ----------------------------------
        if (intent == null
                || intent.getData() == null // intend.getData() は Uriオブジェクトが返ってくる
                || !intent.getData().toString().startsWith(CALLBACK_URL)
                || intent.getData().getQueryParameter("oauth_verifier") == null //キャンセルボタンを押したとき
                ) {
            return;
        }

        // ----------------------------------
        // メイン処理、アクセストークンを取得→SharedPreferenceに保存
        // ----------------------------------
        // https://developer.yahoo.co.jp/other/oauth/flow.html
        new GetAccessTokenAsyncTask(
                this,
                this.getRequestTokenAsyncTask.getRequestToken(),
                intent.getData().getQueryParameter("oauth_verifier")
        ).execute();
    }

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
