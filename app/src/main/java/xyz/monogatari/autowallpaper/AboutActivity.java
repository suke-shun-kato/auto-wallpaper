package xyz.monogatari.autowallpaper;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

/**
 * ライセンス表示用のアクティビティ
 * Created by k-shunsuke on 2018/03/07.
 */
public class AboutActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_about);

        // ----------------------------------
        // アクションバーの設定
        // ----------------------------------
        // ここのActionBar は android.support.v7.app.ActionBa の方のクラスになる
        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
}
