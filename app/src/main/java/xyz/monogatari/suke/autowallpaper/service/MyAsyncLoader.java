package xyz.monogatari.suke.autowallpaper.service;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import com.github.scribejava.apis.TwitterApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth10aService;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import xyz.monogatari.suke.autowallpaper.util.Token;

/**
 * Created by k-shunsuke on 2017/12/27.
 */

public class MyAsyncLoader extends AsyncTaskLoader {

    private Context context;

    public MyAsyncLoader(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public String loadInBackground() {
        try {
            String apiUrl = "https://api.twitter.com/1.1/favorites/list.json";
            final OAuth10aService service
                    = new ServiceBuilder(Token.getTwitterConsumerKey(this.context))
                    .apiSecret(Token.getTwitterConsumerSecret(this.context))
                    .build(TwitterApi.instance());

            final OAuthRequest request = new OAuthRequest(Verb.GET, apiUrl);
            service.signRequest(
                    new OAuth1AccessToken(
                            Token.getTwitterAccessToken(this.context),
                            Token.getTwitterAccessTokenSecret(this.context)
                    ),
                    request
            );
            final Response response = service.execute(request);

            return response.getBody();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }
}
