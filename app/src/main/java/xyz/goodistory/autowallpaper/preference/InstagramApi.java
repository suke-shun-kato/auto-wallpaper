package xyz.goodistory.autowallpaper.preference;

import com.github.scribejava.core.builder.api.DefaultApi20;

public class InstagramApi extends DefaultApi20 {
    @SuppressWarnings("WeakerAccess")
    protected InstagramApi() {
    }

    private static class InstanceHolder {
        private static final InstagramApi INSTANCE = new InstagramApi();
    }

    public static InstagramApi instance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public String getAccessTokenEndpoint() {
        return "https://api.instagram.com/oauth/access_token";
    }

    @Override
    protected String getAuthorizationBaseUrl() {
        return "https://api.instagram.com/oauth/authorize";
    }
}
