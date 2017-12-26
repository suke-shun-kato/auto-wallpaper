package xyz.monogatari.suke.autowallpaper;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import twitter4j.Twitter;
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

    }
}
