package xyz.monogatari.suke.autowallpaper;

import android.util.Log;

import org.jdeferred.AlwaysCallback;
import org.jdeferred.Deferred;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.ProgressCallback;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
//        assertEquals(4, 2 + 2);

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