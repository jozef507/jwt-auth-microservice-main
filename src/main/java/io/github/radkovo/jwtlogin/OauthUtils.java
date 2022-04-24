package io.github.radkovo.jwtlogin;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.http.BasicAuthentication;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.github.radkovo.jwtlogin.dao.OauthAccessTokenService;
import io.github.radkovo.jwtlogin.data.GithubUserInfo;
import io.github.radkovo.jwtlogin.data.GoogleUserInfo;
import io.github.radkovo.jwtlogin.data.OauthAccessToken;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.*;

/**
 * @author xondri05
 *
 */

public class OauthUtils {


    @Inject
    @ConfigProperty(name = "oauth.google.client.id", defaultValue = "")
    private String googleClientId;

    @Inject
    @ConfigProperty(name = "oauth.google.client.secret", defaultValue = "")
    private String googleClientSecret;

    private final List<String> GOOGLE_SCOPES = Arrays.asList(
            "https://www.googleapis.com/auth/userinfo.profile",
            "https://www.googleapis.com/auth/userinfo.email");

    private final String GOOGLE_URL_TOKEN = "https://accounts.google.com/o/oauth2/token";
    private final String GOOGLE_URL_AUTHORIZAON = "https://accounts.google.com/o/oauth2/auth";
    public final String GOOGLE_API_URI = "https://www.googleapis.com/oauth2/v2/userinfo";

    public AuthorizationCodeFlow initializeFlowGoogle() throws IOException {

        return new AuthorizationCodeFlow.Builder(BearerToken.authorizationHeaderAccessMethod(),
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                new GenericUrl(GOOGLE_URL_TOKEN),
                new BasicAuthentication(googleClientId, googleClientSecret), googleClientId, GOOGLE_URL_AUTHORIZAON)
                    .setScopes(GOOGLE_SCOPES).build();
    }

    public GoogleUserInfo getGoogleUserInfo(OauthAccessToken token) throws IOException {

        URL resourceURL = new URL(GOOGLE_API_URI);
        URLConnection conn = resourceURL.openConnection();
        conn.setRequestProperty("Authorization", "Bearer "+token.getAccessToken());
        InputStream response = conn.getInputStream();
        String responseBody;
        try (Scanner scanner = new Scanner(response)) {
            responseBody = scanner.useDelimiter("\\A").next();
        }

        return (new Gson().fromJson(responseBody, GoogleUserInfo.class));
    }

    /////////////////////////////////////////
    // GITHUB SERVICE
    ////////////////////////////////////////

    @Inject
    @ConfigProperty(name = "oauth.github.client.id", defaultValue = "")
    private String githubClientId;

    @Inject
    @ConfigProperty(name = "oauth.github.client.secret", defaultValue = "")
    private String githubClientSecret;

    @Inject
    @ConfigProperty(name = "backend.base.url", defaultValue = "")
    private String backendOauthUrl;

    private final List<String> GITHUB_SCOPES = Arrays.asList(
            "read:user",
            "user:email");

    private final String GITHUB_URL_TOKEN = "https://github.com/login/oauth/access_token";
    private final String GITHUB_URL_AUTHORIZAON = "https://github.com/login/oauth/authorize";
    public final String GITHUB_API_URI = "https://api.github.com/user";

    public AuthorizationCodeFlow initializeFlowGithub() throws IOException {

        return new AuthorizationCodeFlow.Builder(BearerToken.authorizationHeaderAccessMethod(),
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                new GenericUrl(GITHUB_URL_TOKEN),
                new BasicAuthentication(githubClientId, githubClientSecret), githubClientId, GITHUB_URL_AUTHORIZAON)
                    .setScopes(GITHUB_SCOPES).build();
    }

    public String obtainGithubAccessToken(String code) throws IOException {
        URL url = new URL(GITHUB_URL_TOKEN);
        Map<String,Object> params = new LinkedHashMap<>();
        params.put("client_id", "7612bd191fbfe3b29805");
        params.put("client_secret", "484fcbf73a9fe575fe73f85acbcc2d87be46db1d");
        params.put("code", code);
        params.put("redirect_uri", backendOauthUrl+"oauth/github-redirect");

        StringBuilder postData = new StringBuilder();
        for (Map.Entry<String,Object> param : params.entrySet()) {
            if (postData.length() != 0) postData.append('&');
            postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
            postData.append('=');
            postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
        }
        byte[] postDataBytes = postData.toString().getBytes("UTF-8");

        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);
        conn.getOutputStream().write(postDataBytes);

        InputStream response = conn.getInputStream();
        String responseData;
        try (Scanner scanner = new Scanner(response)) {
            responseData = scanner.useDelimiter("\\A").next();
        }

        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(responseData, JsonObject.class);
        String token = jsonObject.get("access_token").toString();
        token = token.substring(1, token.length() - 1);
        return token;
    }

    public GithubUserInfo getGithubUserInfo(OauthAccessToken token) throws IOException {

        URL resourceURL = new URL(GITHUB_API_URI);
        URLConnection conn = resourceURL.openConnection();
        conn.setRequestProperty("Authorization", "Token "+token.getAccessToken());
        InputStream response = conn.getInputStream();
        String responseBody;
        try (Scanner scanner = new Scanner(response)) {
            responseBody = scanner.useDelimiter("\\A").next();
        }

        return (new Gson().fromJson(responseBody, GithubUserInfo.class));
    }



}
