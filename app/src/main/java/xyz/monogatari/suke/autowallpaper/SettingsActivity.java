package xyz.monogatari.suke.autowallpaper;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import xyz.monogatari.suke.autowallpaper.service.MainService;

/**
 * 設定画面のアクティビティ
 * Created by k-shunsuke on 2017/12/08.
 */
public class SettingsActivity extends AppCompatActivity {
    // --------------------------------------------------------------------
    // フィールド
    // --------------------------------------------------------------------


    // --------------------------------------------------------------------
    // メソッド
    // --------------------------------------------------------------------
    /************************************
     * アクティビティが作成されたとき
     * @param savedInstanceState
     */
    @SuppressWarnings("JavaDoc")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
Log.d("○" + this.getClass().getSimpleName(), "onCreate() 呼ばれた: ");
        super.onCreate(savedInstanceState);
Log.d("○" + this.getClass().getSimpleName(), "onCreate() 呼ばれた: super2");
        if ( savedInstanceState == null) {
            this.getFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new SettingsFragment())
                    .commit();
            Log.d("○" + this.getClass().getSimpleName(), "onCreate() 呼ばれた: super3");
        }

    }

//    /************************************
//     * アクティビティの画面が表示状態になるとき
//     */
//    @Override
//    protected void onStart() {
//Log.d("○" + this.getClass().getSimpleName(), "onStart() 呼ばれた");
//        super.onStart();
//    }
//
//    /************************************
//     * 画面が非表示になるとき
//     */
//    @Override
//    protected void onStop() {
//Log.d("○" + this.getClass().getSimpleName(), "onStop() 呼ばれた");
//Log.d("○" + this.getClass().getSimpleName(), this.isBound+"");
//        super.onStop();
//
//    }



    /************************************
     *
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
Log.d("○" + this.getClass().getSimpleName(), "onSaveInstanceState() 呼ばれた");
        super.onSaveInstanceState(outState);
    }
}
