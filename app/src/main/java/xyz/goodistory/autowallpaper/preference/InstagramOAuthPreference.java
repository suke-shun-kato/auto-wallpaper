package xyz.goodistory.autowallpaper.preference;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.github.scribejava.core.builder.ServiceBuilder;
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
     * TODO なぜstaticだとエラーでないか調べる
     */
    public static class AuthorizationActivity extends AppCompatActivity {
        @SuppressLint("SetJavaScriptEnabled")
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            //// いつもの
            super.onCreate(savedInstanceState);
            this.setContentView(R.layout.activity_oauth_authorization);


            Intent intent = getIntent();
            String authorizationUrl = intent.getStringExtra(InstagramOAuthPreference.NAME_URL);

            //// webViewで認証画面を表示する
            WebView webView = findViewById(R.id.oauth_authorization_web);
            webView.setWebViewClient(new WebViewClient());
            webView.getSettings().setJavaScriptEnabled(true);

            // リダイレクトを取得
            webView.setWebViewClient(new WebViewClient() {
//            private boolean aaaa(Uri uri) {
//                if (!"ssss".equals(uri.getHost())) {
//                    return false;
//                }
//
//                return true;
//            }

                /**
                 * API 24 (Android 7.0) ～
                 * @param view WebView
                 * @param request WebResourceRequest
                 * @return true を返すとページ読み込みをしなくなる
                 */
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    boolean isParent = super.shouldOverrideUrlLoading(view, request);


//                Uri uri = request.getUrl();
                    return false;

//                return isParent;

                }

                /**
                 * API 23まで
                 * @param view
                 * @param url
                 * @return
                 */
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {

                    Uri uri = Uri.parse(url);

                    return super.shouldOverrideUrlLoading(view, url);
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
    private final String mClientID;
    private final String mClientSecret;

    // --------------------------------------------------------------------
    // 定数
    // --------------------------------------------------------------------
    // NAME にパッケージ名を入れるべきと書いていた
    public static final String NAME_URL = "xyz.goodistory.autowallpaper.uri";
    private static final String KEY_ACCESS_TOKEN = "access_token";

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
             mCallbackUrl= typedAry.getString(R.styleable.InstagramOAuthPreference_callbackUrl);
             mClientID = typedAry.getString(R.styleable.InstagramOAuthPreference_clientID);
             mClientSecret = typedAry.getString(R.styleable.InstagramOAuthPreference_clientSecret);
        } finally {
            typedAry.recycle();
        }
    }



    /************************************
     * クリックしたら認証ページをWEBプラウザで開く
     * （アプリ内のWebViewで開くことも考えたけど、
     * WEBプラウザなどではクッキーが使えるので敢えてWEBプラウザにした）
     */
    @Override
    protected void onClick() {
        //// 認証ページのURLを取得
        final OAuth20Service service = new ServiceBuilder(mClientID)
                .callback("http://autowallpaper.goodistory.xyz/instagram/authorization") // TODO ちゃんとする、XMLから
                .responseType("code")
                .build(InstagramApi.instance());

        final String authorizationUrl = service.getAuthorizationUrl();

        //// 認証ページをブラウザで開く
        Intent intent = new Intent(getContext(), AuthorizationActivity.class);
        intent.putExtra(NAME_URL, authorizationUrl);
        getContext().startActivity(intent);
    }
}
