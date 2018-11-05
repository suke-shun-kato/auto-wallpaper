package xyz.goodistory.autowallpaper;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

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
        Toolbar myToolbar = findViewById(R.id.my_toolbar);


        ////　ツールバーをアクションバーとして表示
        this.setSupportActionBar(myToolbar);


        //// タイトルを動的に編集
        CharSequence title = myToolbar.getTitle();
        myToolbar.setTitle(
            String.format(title.toString(), this.getString(R.string.app_name))
        );


        //// アクションバーに「←」ボタンを表示、
        // 詳しくはHistoryActivityの同じ部分を参照
        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // ----------------------------------
        // バージョンのテキストを動的にセット
        // ----------------------------------
        TextView tx = this.findViewById(R.id.about_version);
        tx.setText(
                String.format(getString(R.string.about_version), this.getVersionName())
        );

        // ----------------------------------
        // ライセンス部分のListView
        // ----------------------------------
        ListView lv = findViewById(R.id.about_license_list);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.item_list_about_license,
                this.getResources().getStringArray(R.array.about_license_bodies)
        );
        lv.setAdapter(adapter);

    }

//    /************************************
//     * ボタンを押したときの制御
//     */
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch(item.getItemId()) {
//            case android.R.id.home: //押したボタンがアクションバーの←ボタンだとすると
//                finish();
//                return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    /**
     * バージョン名を取得する
     *
     * @return バージョン名
     */
    private String getVersionName() {
        String versionName = "";
        try {
            PackageInfo packageInfo = this.getPackageManager().getPackageInfo(this.getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }
}
