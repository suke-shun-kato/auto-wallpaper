package xyz.monogatari.autowallpaper;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * ライセンス表示用のアクティビティ
 * Created by k-shunsuke on 2018/03/07.
 */
public class LicenseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_license);
    }
}
