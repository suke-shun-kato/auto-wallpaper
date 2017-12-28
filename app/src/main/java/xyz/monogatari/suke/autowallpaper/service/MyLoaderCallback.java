package xyz.monogatari.suke.autowallpaper.service;

import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by k-shunsuke on 2017/12/27.
 */

public class MyLoaderCallback implements LoaderManager.LoaderCallbacks<String> {

    @Override
    public Loader<String> onCreateLoader(int id, Bundle bundle) {
        if (id == 1) {
            return  new MyAsyncLoader(context);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String jsonStr) {
        Log.d("○"+this.getClass().getSimpleName(), jsonStr);
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {

    }
}
