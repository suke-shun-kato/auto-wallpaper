package xyz.monogatari.suke.autowallpaper;

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

public class TwitterOAuthPreference extends Preference {
    // --------------------------------------------------------------------
    // フィールド
    // --------------------------------------------------------------------
    /** リクエストトークン、Twitter認証用、twitter4J */
    private RequestToken requestToken;
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
    public static final String CALLBACK_URL = "android-suke://twitter";

    public static final String KEY_TOKEN = "token";
    public static final String KEY_TOKEN_SECRET = "token_secret";

    //認証ページ開けれない
//    R.string.setting_from_twitter_oauth_fail

    // 認証ページ後、アクセストークン取得できない
//    R.string.setting_from_twitter_oauth_toast_oauthOk
//    R.string.setting_from_twitter_oauth_toast_oauthNo
    // --------------------------------------------------------------------
    // コンストラクタ
    // --------------------------------------------------------------------
    public TwitterOAuthPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        // ----------------------------------
        // XMLのカスタム属性をフィールドに読み込む
        // ----------------------------------
        TypedArray typedAry = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.TwitterOAuthPreference,
                0, 0);
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
        private TwitterOAuthPreference twPreference;
        private RequestToken requestToken;

        /************************************
         * コンストラクタ
         */
        public GetRequestTokenAsyncTask(TwitterOAuthPreference twPreference, RequestToken requestToken) {
            super();
            this.twPreference = twPreference;
            this.requestToken = requestToken;
        }

        /************************************
         * RequestTokenの取得→それを利用して認証画面のURLを生成
         * WEBにアクセスして時間がかかるので非同期処理
         * @return Twitterの認証画面のURL
         */
        @Override
        protected RequestToken doInBackground(Void... params) {
            try {
//Log.d("○△", "ブロックの前");
                RequestToken requestToken = this.twPreference.twitter.getOAuthRequestToken(CALLBACK_URL);
//Log.d("○△", requestToken.toString());
                return requestToken;
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("○○○",e.getMessage());
            }
//Log.d("○△", "ブロックの後");
            return null;
        }

        /************************************
         * 非同期処理の結果処理、認証のWEBページを開く
         * @param requestToken リクエストトークン
         */
        @Override
        protected void onPostExecute(RequestToken requestToken) {
            this.requestToken = requestToken;

            String url = this.requestToken!=null ? this.requestToken.getAuthorizationURL() : null;
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
        this.requestToken = new RequestToken("","");

        //// コンシューマーキー、コンシューマーシークレットのセット
        this.twitter.setOAuthConsumer(
                this.getContext().getString(R.string.twitter_consumer_key),
                this.getContext().getString(R.string.twitter_consumer_secret)
        );

        //// 非同期でアクセストークン取得する
//        new GetRequestTokenAsyncTask(this, this.requestToken).execute();
        this.getRequestTokenAsyncTask = new GetRequestTokenAsyncTask(this, this.requestToken);
        getRequestTokenAsyncTask.execute();
    }
///////////////////////////////////////////////
    private static class GetAccessTokenAsyncTask extends  AsyncTask<Void, Void, AccessToken> {
        private TwitterOAuthPreference twPreference;
        private RequestToken requestToken;
        private String verifierStr;

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
// accessToken=null;
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
Log.d("○"+getClass().getSimpleName(), "onNewIntent():top");
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
Log.d("○"+getClass().getSimpleName(), "intentのURI: "+intent.getData().toString());

        // ----------------------------------
        // メイン処理、アクセストークンを取得→SharedPreferenceに保存
        // ----------------------------------
        // https://developer.yahoo.co.jp/other/oauth/flow.html
        new GetAccessTokenAsyncTask(
                this,
                this.getRequestTokenAsyncTask.requestToken,
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
