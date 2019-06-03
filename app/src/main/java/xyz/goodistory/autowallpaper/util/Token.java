package xyz.goodistory.autowallpaper.util;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import xyz.goodistory.autowallpaper.R;
import xyz.goodistory.autowallpaper.SettingsFragment;
import xyz.goodistory.autowallpaper.preference.TwitterOAuthPreference;

/**
 * TODO SettingFragment に統合したい
 * API認証のためのトークンを取得するクラス
 * Created by k-shunsuke on 2017/12/27.
 */

public class Token {

    public static String getTwitterConsumerKey(Context context) {
        return context.getString(R.string.twitter_consumer_key);
    }
    public static String getTwitterConsumerSecret(Context context) {
        return context.getString(R.string.twitter_consumer_secret);
    }

    @Nullable
    public static String getTwitterAccessToken(Context context) {
        try {
            JSONObject tokenJson = new JSONObject(
                    PreferenceManager.getDefaultSharedPreferences(context)
                            .getString(SettingsFragment.KEY_FROM_TWITTER_OAUTH, null)
            );
            return tokenJson.getString(TwitterOAuthPreference.KEY_TOKEN);
        } catch (JSONException e) {
            Log.w("○"+ context.getClass().getSimpleName(), e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    public static String getTwitterAccessTokenSecret(Context context) {
        try {
            JSONObject tokenJson = new JSONObject(
                    PreferenceManager.getDefaultSharedPreferences(context)
                            .getString(SettingsFragment.KEY_FROM_TWITTER_OAUTH, null)
            );
            return tokenJson.getString(TwitterOAuthPreference.KEY_TOKEN_SECRET);
        } catch (JSONException e) {
            return null;
        }
    }

}
