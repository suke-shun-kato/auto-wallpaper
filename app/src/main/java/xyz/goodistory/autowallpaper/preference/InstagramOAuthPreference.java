package xyz.goodistory.autowallpaper.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;
import android.widget.Toast;

import xyz.goodistory.autowallpaper.R;

/**
 * Instagram のOAuth認証を行う Preference
 * https://www.instagram.com/developer/
 */
public class InstagramOAuthPreference extends Preference {
    // --------------------------------------------------------------------
    // フィールド
    // --------------------------------------------------------------------
    /** 認証ボタンを押した後のコールバックURL、アクセストークン取得 */
    private final String mCallbackUrl;
    private final String mConsumerKey;
    private final String mConsumerSecret;

    // --------------------------------------------------------------------
    // 定数
    // --------------------------------------------------------------------
    private static final String KEY_TOKEN = "token";
    private static final String KEY_TOKEN_SECRET = "token_secret";

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
             mConsumerKey = typedAry.getString(R.styleable.InstagramOAuthPreference_consumerKey);
             mConsumerSecret = typedAry.getString(R.styleable.InstagramOAuthPreference_consumerSecret);
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




        Toast.makeText(getContext(),
                mCallbackUrl + ":" + mConsumerKey + ":" + mConsumerSecret,
                Toast.LENGTH_LONG).show();
    }
}
