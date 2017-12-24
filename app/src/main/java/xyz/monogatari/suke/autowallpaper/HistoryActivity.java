package xyz.monogatari.suke.autowallpaper;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.RequestToken;

/**
 * 履歴ページ、ひとまず作成
 * Created by k-shunsuke on 2017/12/20.
 */

public class HistoryActivity extends AppCompatActivity {
    // --------------------------------------------------------------------
    // フィールド
    // --------------------------------------------------------------------
//    private String mCallbackURL;
    /** twitter4Jの大本のクラス */
    private Twitter mTwitter;
    private RequestToken mRequestToken;

    // --------------------------------------------------------------------
    // メソッド（オーバーライド）
    // --------------------------------------------------------------------
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_history);

//        mCallbackURL = getString(R.string.twitter_callback_url);
//        this.mTwitter = TwitterUtils.getTwitterInstance(this);



        AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    HistoryActivity.this.mRequestToken
                            = HistoryActivity.this.mTwitter.getOAuthRequestToken();
                    return HistoryActivity.this.mRequestToken.getAuthorizationURL();
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String urlStr) {
                if (urlStr != null) {
//                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlStr));
//                    startActivity(intent);

                    WebView webView = findViewById(R.id.history_webView);
                    webView.loadUrl(urlStr);
                } else {
                    // 失敗。。。
                }
            }
        };
        task.execute();



    }


}
