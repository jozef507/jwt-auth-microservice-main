package io.github.radkovo.jwtlogin;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.http.BasicAuthentication;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.client.json.gson.GsonFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class OauthUtils {

    public static AuthorizationCodeFlow initializeFlow() throws IOException {
        List<String> scopes = Arrays.asList(
                "https://www.googleapis.com/auth/userinfo.profile",
                "https://www.googleapis.com/auth/userinfo.email");

        return new AuthorizationCodeFlow.Builder(BearerToken.authorizationHeaderAccessMethod(),
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                new GenericUrl(" https://accounts.google.com/o/oauth2/token"),
                new BasicAuthentication("333354051207-9d43okp01u89j8iovbtim50qmskojd93.apps.googleusercontent.com", "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"),
                "333354051207-9d43okp01u89j8iovbtim50qmskojd93.apps.googleusercontent.com",
                "https://accounts.google.com/o/oauth2/auth").setCredentialDataStore(
                        StoredCredential.getDefaultDataStore(
                                new FileDataStoreFactory(new File("datastoredir"))))
                .setScopes(scopes).build();
    }


}
