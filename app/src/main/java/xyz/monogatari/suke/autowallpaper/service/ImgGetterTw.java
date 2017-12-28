package xyz.monogatari.suke.autowallpaper.service;

import android.app.LoaderManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;

import com.github.scribejava.apis.TwitterApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth10aService;

import org.jdeferred.AlwaysCallback;
import org.jdeferred.Deferred;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.ProgressCallback;
import org.jdeferred.Promise;
import org.jdeferred.android.AndroidDeferredManager;
import org.jdeferred.impl.DeferredObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.transform.Result;

import xyz.monogatari.suke.autowallpaper.MainActivity;
import xyz.monogatari.suke.autowallpaper.util.Token;

/**
 * Created by k-shunsuke on 2017/12/27.
 */

public class ImgGetterTw implements ImgGetter {
    private Context context;

    public ImgGetterTw(Context context) {
        this.context = context;
    }

    public Bitmap getImg() {                              //引数
        // p.then()                                        DoneFilter, DonePipe
        // dm.when()                (p,p,p), Runnable,Callable,DeferredRunnable,DeferredCallable
        //                           ,DeferredAsyncTask, DeferredFutureTask
        //
        // 一つのdeferredにつき一つのPromise??, whenとthenは同じ
        // p.done() 成功     deferred.resolve(...)               DoneCallback
        // p.fail() 失敗     deferred.reject(new Exception());   FailCallback
        // p.progress() 進捗 deferred.notify(...)                ProgressCallback
        // p.always()  全て                                      AlwaysCallback
        //
        // 通常はPromise(Deferred.promise())オブジェクト、
        //                DeferredManager(new DefaultDeferredManager())オブジェクト
        // Androidの場合はAndroidDeferredManager(new AndroidDeferredManager(ExecutorService))オブジェクト


        Deferred deferred = new DeferredObject();
        Promise promise = deferred.promise();

        promise.done(new DoneCallback<String>() {
            @Override
            public void onDone(String result) {
                Log.d("○△", "done(): result:" + result);
            }
        }).fail(new FailCallback<Throwable>() {
            @Override
            public void onFail(Throwable rejection) {
                Log.d("○△", "fail(): rejection:" + rejection);
            }
        }).progress(new ProgressCallback<String>() {
            @Override
            public void onProgress(String progress) {
                Log.d("○△", "progress()");
            }
        }).always(new AlwaysCallback<String, Throwable>() {
            @Override
            public void onAlways(Promise.State state, String result, Throwable rejection) {
                Log.d("○△", "always(): state:"+state+ " result:"+result + " rejection:"+rejection);
            }
        });
        deferred.resolve(100);
    }
}