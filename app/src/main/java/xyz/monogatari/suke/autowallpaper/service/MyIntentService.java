package xyz.monogatari.suke.autowallpaper.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

/**
 * Created by k-shunsuke on 2017/12/27.
 */

public class MyIntentService extends IntentService {
    public MyIntentService(String name) {
        super(name);
    }
    public MyIntentService() {
        super();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }
}
