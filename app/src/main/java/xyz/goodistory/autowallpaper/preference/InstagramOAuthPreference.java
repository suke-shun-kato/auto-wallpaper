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
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

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
            webView.setWebViewClient(new WebViewClient());
            webView.getSettings().setJavaScriptEnabled(true);

            // リダイレクトを取得
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
    private final String mClientID;
    private final String mClientSecret;
    private final OAuth20Service mOAuth20Service;

    // --------------------------------------------------------------------
    // 定数
    // --------------------------------------------------------------------
    // NAME にパッケージ名を入れるべきと書いていた
    private static final String INTENT_NAME_AUTHORIZATION = "xyz.goodistory.autowallpaper.authorization";
    private static final String INTENT_NAME_CALLBACK = "xyz.goodistory.autowallpaper.callback";

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
            mCallbackUrl= typedAry.getString(R.styleable.InstagramOAuthPreference_callbackUrl);
            mClientID = typedAry.getString(R.styleable.InstagramOAuthPreference_clientID);
            mClientSecret = typedAry.getString(R.styleable.InstagramOAuthPreference_clientSecret);

            // OAuth20Service を作成
            mOAuth20Service = new ServiceBuilder(mClientID)
                    .apiSecret(mClientSecret)
                    .callback(mCallbackUrl)
                    .responseType("code")
                    .build(InstagramApi.instance());
        } finally {
            typedAry.recycle();
        }
    }

    // --------------------------------------------------------------------
    // メソッド
    // --------------------------------------------------------------------
    /************************************
     * TODO Preference専用のあるかも
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
            Uri uri = intent.getData();
            if (uri == null ) {
                // TODO エラーメッセージをリソース化
                Toast.makeText(
                        getContext(), "認可コードの取得に失敗しました", Toast.LENGTH_LONG
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
                    // TODO リソース化
                    Toast.makeText(
                            getContext(), "アクセストークンの取得に失敗しました", Toast.LENGTH_LONG
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
                        getContext(), "認証に成功しました", Toast.LENGTH_LONG
                ).show();
            }

            /**
             * キャンセル時の処理
             */
            @Override
            protected void onCancelled() {
                Toast.makeText(
                        getContext(), "アクセストークンの取得に失敗しました", Toast.LENGTH_LONG
                ).show();
            }
        }
    }

}
