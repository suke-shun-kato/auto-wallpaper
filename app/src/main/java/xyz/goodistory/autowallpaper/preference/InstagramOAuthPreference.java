package xyz.goodistory.autowallpaper.preference;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.PatternMatcher;
import android.preference.Preference;
import androidx.appcompat.app.AppCompatActivity;
import android.util.AttributeSet;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.github.scribejava.apis.InstagramApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;

import xyz.goodistory.autowallpaper.R;

/**
 * Instagram のOAuth認証を行う Preference
 * https://www.instagram.com/developer/
 */
public class InstagramOAuthPreference extends Preference {
    // --------------------------------------------------------------------
    // 内部クラス
    // --------------------------------------------------------------------
    /**
     * アクティビティはstaticでないと作れなかった
     */
    public static class AuthorizationActivity extends AppCompatActivity {
        @SuppressLint("SetJavaScriptEnabled")
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            //// いつもの
            super.onCreate(savedInstanceState);
            this.setContentView(R.layout.activity_oauth_authorization);

            //// 前処理
            final Intent intent = getIntent();
            final String authorizationUrl = intent.getStringExtra(
                    InstagramOAuthPreference.INTENT_NAME_AUTHORIZATION);
            final String callbackUrl = intent.getStringExtra(
                    InstagramOAuthPreference.INTENT_NAME_CALLBACK);

            //// webViewで認証画面を表示する
            WebView webView = findViewById(R.id.oauth_authorization_web);

            // javascriptを有効にする、instagram の認証画面はJSを有効しないと動かない
            webView.getSettings().setJavaScriptEnabled(true);

            // クッキーを全て削除する（セッション削除目的）
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { // API 21, Android 5.0以上
                CookieManager cookieManager = CookieManager.getInstance();

                cookieManager.removeAllCookies(null);
                cookieManager.flush();
            } else {
                CookieSyncManager cookieSyncManager = CookieSyncManager.createInstance(this);
                cookieSyncManager.startSync();

                CookieManager cookieManager = CookieManager.getInstance();
                cookieManager.removeAllCookie();
                cookieManager.removeSessionCookie();

                cookieSyncManager.stopSync();
                cookieSyncManager.sync();
            }

            // コールバックのリダイレクトを取得
            webView.setWebViewClient(new WebViewClient() {
                private final String macCallbackUrl = callbackUrl;

                /**
                 * 指定のUri がcallbackUrlかどうか
                 * @param targetUri チェックするUri
                 * @return true: callbackUrlと等しい
                 */
                @SuppressWarnings("RedundantIfStatement")
                private boolean isCallbackUrl(Uri targetUri) {
                    Uri expectedUri = Uri.parse(macCallbackUrl);

                    String eScheme = expectedUri.getScheme();
                    String tScheme = targetUri.getScheme();
                    String eHost = expectedUri.getHost();
                    String tHost = targetUri.getHost();
                    String ePath = expectedUri.getPath();
                    String tPath = targetUri.getPath();

                    if (eScheme == null || !eScheme.equals(tScheme)) {
                        return false;
                    }
                    if (eHost == null || !eHost.equals(tHost)) {
                        return false;
                    }
                    if (ePath == null || !ePath.equals(tPath)) {
                        return false;
                    }

                    return true;
                }

                /**
                 * API 24 (Android 7.0, Nougat) ～
                 *
                 * @param view    The WebView that is initiating the callback.
                 * @param request Object containing the details of the request.
                 * @return True if the host application wants to leave the current WebView
                 * and handle the url itself, otherwise return false.
                 */
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public boolean shouldOverrideUrlLoading(
                        WebView view, WebResourceRequest request) {
                    Uri uri = request.getUrl();
                    boolean isCallbackUrl = isCallbackUrl(uri);

                    if (isCallbackUrl) {
                        Intent sendIntent = new Intent(Intent.ACTION_VIEW, uri);
                        sendBroadcast(sendIntent);
                        finish();
                    }

                    return isCallbackUrl;
                }

                /**
                 * @param view The WebView that is initiating the callback.
                 * @param url  The url to be loaded.
                 * @return True if the host application wants to leave the current WebView
                 * and handle the url itself, otherwise return false.
                 * @deprecated Use {@link #shouldOverrideUrlLoading(WebView, WebResourceRequest)
                 * shouldOverrideUrlLoading(WebView, WebResourceRequest)} instead.
                 */
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    boolean isCallbackUrl = isCallbackUrl(Uri.parse(url));

                    if (isCallbackUrl) {
                        Intent sendIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        sendBroadcast(sendIntent);
                        finish();
                    }
                    return isCallbackUrl;
                }
            });

            // 認証画面を表示
            webView.loadUrl(authorizationUrl);
        }
    }
    // --------------------------------------------------------------------
    // フィールド
    // --------------------------------------------------------------------
    /** 認証ボタンを押した後のコールバックURL、アクセストークン取得 */
    private final String mCallbackUrl;
    private CallbackBroadcastReceiver mCallbackBroadcastReceiver;
    private final OAuth20Service mOAuth20Service;
    private final String mSummaryDone;
    private final String mSummaryNotYet;

    // --------------------------------------------------------------------
    // 定数
    // --------------------------------------------------------------------
    /** 対応バージョンコード */
    @SuppressWarnings("WeakerAccess")
    public static final int SUPPORTED_API_LEVEL = Build.VERSION_CODES.LOLLIPOP;

    // NAME にパッケージ名を入れるべきと書いていた
    private static final String INTENT_NAME_AUTHORIZATION = "xyz.goodistory.autowallpaper.authorization";
    private static final String INTENT_NAME_CALLBACK = "xyz.goodistory.autowallpaper.callback";

    private static final int SUMMARY_DONE = 1;
    private static final int SUMMARY_NOT_YET = 2;
    private static final int SUMMARY_NOT_SUPPORTED = 3;

    private static final String TEXT_NOT_SUPPORTED = "This android version is not supported";

    // --------------------------------------------------------------------
    // コンストラクタ
    // --------------------------------------------------------------------
    /**
     * コンストラクタ
     * @param context Context
     * @param attrs AttributeSet
     */
    public InstagramOAuthPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        // ----------------------------------
        // XMLのカスタム属性をフィールドに読み込む
        // ----------------------------------
        TypedArray typedAry = context.obtainStyledAttributes(attrs, R.styleable.InstagramOAuthPreference);

        try {
            //// XMLから読み込む
            final String clientID
                    = typedAry.getString( R.styleable.InstagramOAuthPreference_clientID );
            final String clientSecret
                    = typedAry.getString( R.styleable.InstagramOAuthPreference_clientSecret );

            mCallbackUrl= typedAry.getString(R.styleable.InstagramOAuthPreference_callbackUrl);
            mSummaryDone = typedAry.getString(R.styleable.InstagramOAuthPreference_summaryDone);
            mSummaryNotYet = typedAry.getString(R.styleable.InstagramOAuthPreference_summaryNotYet);

            // OAuth20Service を作成
            mOAuth20Service = new ServiceBuilder(clientID)
                    .apiSecret(clientSecret)
                    .callback(mCallbackUrl)
                    .responseType("code")
                    .build(InstagramApi.instance());
        } finally {
            typedAry.recycle();
        }

        // ----------------------------------
        // API level による使用制限
        // ----------------------------------
        if (Build.VERSION.SDK_INT < SUPPORTED_API_LEVEL) {
            setEnabled(false);
        }

    }

    // --------------------------------------------------------------------
    // メソッド
    // --------------------------------------------------------------------

    /**
     * プリファレンスにトークンが保存
     */
    public void updateSummary() {
        if (Build.VERSION.SDK_INT < SUPPORTED_API_LEVEL) {
            setInstagramSummary(SUMMARY_NOT_SUPPORTED);
            return;
        }

        if ( isDoneAuthorization() ) {
            setInstagramSummary(SUMMARY_DONE);
        } else {
            setInstagramSummary(SUMMARY_NOT_YET);
        }
    }

    /**
     * アクセストークン取得済か
     * @return 取得済のときtrue
     */
    private boolean isDoneAuthorization() {
        String accessToken = getPersistedString("");

        return !accessToken.equals("");
    }


    /**
     * サマリーをセットする
     * @param kindSummary セットするサマリーの種類
     */
    private void setInstagramSummary(int kindSummary) {
        String summaryText;
        switch (kindSummary) {
            case SUMMARY_DONE:
                summaryText = mSummaryDone;
                break;
            case SUMMARY_NOT_YET:
                summaryText = mSummaryNotYet;
                break;
            case SUMMARY_NOT_SUPPORTED:
                summaryText = TEXT_NOT_SUPPORTED;
                break;
            default:
                throw new IllegalArgumentException(
                        "第1引数の値が不正です");
        }

        setSummary(summaryText);
    }

    // --------------------------------------------------------------------
    // メソッド、オーバーライド
    // --------------------------------------------------------------------

    /************************************
     * ブロードキャストレシーバーのセット
     * クリックしたら認証ページをWEBプラウザで開く
     * （アプリ内のWebViewで開くことも考えたけど、
     * WEBプラウザなどではクッキーが使えるので敢えてWEBプラウザにした）
     */
    @Override
    protected void onClick() {
        // ----------------------------------
        // BroadcastReceiver をセット
        // ----------------------------------
        //// broadcastReceiver
        mCallbackBroadcastReceiver = new CallbackBroadcastReceiver();

        //// intentFilterの設定
        IntentFilter intentFilter = new IntentFilter();

        // action
        intentFilter.addAction(Intent.ACTION_VIEW);

        // category
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);

        // data(uri)
        Uri callbackUri = Uri.parse(mCallbackUrl);
        intentFilter.addDataScheme( callbackUri.getScheme() );
        intentFilter.addDataAuthority(callbackUri.getHost(), String.valueOf(callbackUri.getPort()));
        intentFilter.addDataPath(callbackUri.getPath(), PatternMatcher.PATTERN_LITERAL);

        //// 登録
        getContext().registerReceiver(mCallbackBroadcastReceiver, intentFilter);


        // ----------------------------------
        // 認証ページをブラウザで開く
        // ----------------------------------
        Intent intent = new Intent(getContext(), AuthorizationActivity.class);

        // 認証ページのURLを取得
        String authorizationUrl = mOAuth20Service.getAuthorizationUrl();

        // 認証ページをput
        intent.putExtra(INTENT_NAME_AUTHORIZATION, authorizationUrl);
        intent.putExtra(INTENT_NAME_CALLBACK, mCallbackUrl);

        // WEBビューのアクティビティを表示
        getContext().startActivity(intent);
    }

    /**
     * Instagramからのコールバックをブロードキャストしたものを受け取る
     */
    public class CallbackBroadcastReceiver extends BroadcastReceiver {
        /**
         * @param context The Context in which the receiver is running.
         * @param intent  The Intent being received.
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            //// BroadcastReceiver解除
            context.unregisterReceiver(mCallbackBroadcastReceiver);

            //// 認可コード取得、ブロードキャストで受信したデータから取得
            // TODO 属性から取得する
            Uri uri = intent.getData();
            if (uri == null ) {
                Toast.makeText(
                        getContext(),
                        R.string.preference_error_msg_instagram_failed_get_code,
                        Toast.LENGTH_LONG
                ).show();
                return;
            }
            String code = uri.getQueryParameter("code");

            //// アクセストークンを取得、非同期
            (new GetAccessTokenAsyncTask()).execute(code);
        }

        /**
         * ブロードキャストレシーバー内でAccessTokenを非同期で取得＆プリファレンスに保存するクラス
         * CallbackBroadcastReceiver が非static なのでstaticクラスにはできない
         */
        @SuppressLint("StaticFieldLeak")
        private class GetAccessTokenAsyncTask extends AsyncTask<String, Void, Void> {
            /**
             * @param strings 第一引数: 認可コード
             */
            @Override
            protected Void doInBackground(String... strings) {
                // ----------------------------------
                // アクセストークンを取得
                // ----------------------------------
                //// 前処理
                String code = strings[0];
                String accessToken = null;

                //// アクセストークンを取得
                try {
                    OAuth2AccessToken resOAuth2AccessToken = mOAuth20Service.getAccessToken(code);
                    accessToken = resOAuth2AccessToken.getAccessToken();
                } catch (Exception e) {
                    Toast.makeText(
                            getContext(),
                            R.string.preference_error_msg_instagram_failed_get_code,
                            Toast.LENGTH_LONG
                    ).show();
                }

                // ----------------------------------
                // アクセストークンを保存
                // ----------------------------------
                persistString(accessToken);

                return null;
            }

            /**
             * 非同期処理が終了後の処理、UIスレッドで実行
             */
            @Override
            protected void onPostExecute(Void aVoid) {
                Toast.makeText(
                        getContext(),
                        R.string.preference_error_msg_instagram_succeeded_auth,
                        Toast.LENGTH_LONG
                ).show();
            }

            /**
             * キャンセル時の処理
             */
            @Override
            protected void onCancelled() {
                Toast.makeText(
                        getContext(),
                        R.string.preference_error_msg_instagram_failed_get_token,
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }

}
