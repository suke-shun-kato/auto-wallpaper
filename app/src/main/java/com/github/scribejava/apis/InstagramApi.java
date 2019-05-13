package com.github.scribejava.apis;

import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.builder.api.OAuth2SignatureType;

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

    @Override
    public OAuth2SignatureType getSignatureType() {
        return OAuth2SignatureType.BEARER_URI_QUERY_PARAMETER;
    }
}
