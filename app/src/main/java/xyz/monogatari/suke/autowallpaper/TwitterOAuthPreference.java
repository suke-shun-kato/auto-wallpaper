package xyz.monogatari.suke.autowallpaper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
    }



    // --------------------------------------------------------------------
    // メソッド
    // --------------------------------------------------------------------
    /************************************
     * クリックしたらTwitterの認証ページをWEBプラウザで開く
     * （アプリ内のWebViewで開くことも考えたけど、W
     * EBプラウザなどではクッキーが使えるので敢えてWEBプラウザにした）
     */
    @Override
    protected void onClick() {
        AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
            /************************************
             * RequestTokenの取得→それを利用して認証画面のURLを生成
             * WEBにアクセスして時間がかかるので非同期処理
             * @return Twitterの認証画面のURL
             */
            @Override
            protected String doInBackground(Void... params) {
                try {
//Log.d("○△", "ブロックの前");
                    // これをonCreateView()で実行すると、
                    // 認証画面から戻る押して再度クリックしたときエラーになるのでこの場所でする
                    setTwitterInstance();
                    TwitterOAuthPreference.this.requestToken
                            = TwitterOAuthPreference.this.twitter.getOAuthRequestToken(CALLBACK_URL);
//Log.d("○△", requestToken.toString());
                    return TwitterOAuthPreference.this.requestToken.getAuthorizationURL();
//                } catch (TwitterException e) {
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("○○○",e.getMessage());
                }
//Log.d("○△", "ブロックの後");
                return null;
            }


            /************************************
             * 非同期処理の結果処理、認証のWEBページを開く
             * @param url 認証ページのURL
             */
            @Override
            protected void onPostExecute(String url) {
                if (url != null) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    TwitterOAuthPreference.this.getContext().startActivity(intent);
                } else {
                    Toast.makeText(getContext(), R.string.setting_from_twitter_oauth_fail, Toast.LENGTH_SHORT)
                            .show();
                }
            }
        };
        task.execute();
    }

    /************************************
     * twitterオブジェクト（twitter4J）を作成してフィールドにセット
     * onCreateView()でこれを実行すると何回もセットすることになるのでコンストラクタのタイミングでセット
     */
    private void setTwitterInstance() {
        // ----------------------------------
        // Twitterクラスを作成
        // ----------------------------------
        this.twitter = new TwitterFactory().getInstance();

        // ----------------------------------
        // Twitterクラスにパラメータを設定
        // ----------------------------------
        //// コンシューマーキー、コンシューマーシークレットのセット
        this.twitter.setOAuthConsumer(
                this.getContext().getString(R.string.twitter_consumer_key),
                this.getContext().getString(R.string.twitter_consumer_secret)
        );
    }

//    @Override
//    protected void onAttachedToActivity() {
//Log.d("○"+this.getClass().getSimpleName(), "onAttachedToActivity(): top");
//        super.onAttachedToActivity();
//    }
//
//    @Override
//    protected void onAttachedToHierarchy(PreferenceManager preferenceManager) {
//Log.d("○"+this.getClass().getSimpleName(), "onAttachedToHierarchy(): top");
//        super.onAttachedToHierarchy(preferenceManager);
//    }
//
//    @Override
//    protected void onBindView(View view) {
//Log.d("○"+this.getClass().getSimpleName(), "onBindView(): top");
//        super.onBindView(view);
//    }
//
//    @Override
//    protected void onRestoreInstanceState(Parcelable state) {
//Log.d("○"+this.getClass().getSimpleName(), "onRestoreInstanceState(): top");
//        super.onRestoreInstanceState(state);
//    }
//
//    @Override
//    protected Parcelable onSaveInstanceState() {
//Log.d("○"+this.getClass().getSimpleName(), "onSaveInstanceState(): top");
//        return super.onSaveInstanceState();
//    }
//
//    @Override
//    protected void onPrepareForRemoval() {
//Log.d("○"+this.getClass().getSimpleName(), "onPrepareForRemoval(): top");
//        super.onPrepareForRemoval();
//    }


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
                || !intent.getData().toString().startsWith(this.CALLBACK_URL)
                || intent.getData().getQueryParameter("oauth_verifier") == null //キャンセルボタンを押したとき
                ) {
            return;
        }
Log.d("○"+getClass().getSimpleName(), "intentのURI: "+intent.getData().toString());

        // ----------------------------------
        // メイン処理、アクセストークンを取得→SharedPreferenceに保存
        // ----------------------------------
        // https://developer.yahoo.co.jp/other/oauth/flow.html
        String verifier = intent.getData().getQueryParameter("oauth_verifier");
Log.d("○"+getClass().getSimpleName(), "verifier: "+verifier);

        AsyncTask<String, Void, AccessToken> task = new AsyncTask<String, Void, AccessToken>() {
            /************************************
             * verifierとRequestTokenでAccessTokenをTwitterから取得
             * @param params [0]:verifier
             * @return AccessTokenオブジェクト、twitter4J
             */
            @Override
            protected AccessToken doInBackground(String... params) {
                try {
                    return twitter.getOAuthAccessToken(requestToken, params[0]);
                } catch (TwitterException e) {
                    e.printStackTrace();
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
                    Toast.makeText(TwitterOAuthPreference.this.getContext(),
                            R.string.setting_from_twitter_oauth_toast_oauthOk,
                            Toast.LENGTH_SHORT
                    ).show();

                    //アクセストークンを保存
                    try{
                        JSONObject aTokenJson = new JSONObject()
                                .put(KEY_TOKEN, accessToken.getToken())
                                .put(KEY_TOKEN_SECRET, accessToken.getTokenSecret());
                        persistString(aTokenJson.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    // 認証失敗。。。
                    Toast.makeText(TwitterOAuthPreference.this.getContext(),
                            R.string.setting_from_twitter_oauth_toast_oauthNo,
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }
        };
        task.execute(verifier);
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
