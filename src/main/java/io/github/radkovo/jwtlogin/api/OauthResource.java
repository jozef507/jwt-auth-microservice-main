package io.github.radkovo.jwtlogin.api;

import javax.annotation.security.PermitAll;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import com.google.api.client.auth.oauth2.AuthorizationCodeTokenRequest;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.auth.oauth2.TokenResponse;
import io.github.radkovo.jwtlogin.OauthUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.ws.rs.core.Response;


@Path("oauth")
public class OauthResource {

    @GET
    public String ping() {
        return "Ping OK";
    }

    @GET
    @Path("google")
    public Response google() throws IOException, URISyntaxException {
        URI url = new URI(OauthUtils.initializeFlow().newAuthorizationUrl().setState("xyz")
                .setRedirectUri("http://localhost:8088/jwt-auth/oauth/google1").build());
        Response r = Response.temporaryRedirect(url)./*header("Access-Control-Allow-Origin",url)*/build();
        return r;
    }

    @GET
    @Path("google1")
    public Response google1(@QueryParam("state") String state, @QueryParam("code") String code, @QueryParam("authuser") String authuser
            , @QueryParam("prompt") String prompt, @QueryParam("scope") String scope) throws IOException, TokenResponseException {
        if (code != null)
        {
            TokenResponse token = OauthUtils.initializeFlow().newTokenRequest(code).setRedirectUri("http://localhost:8088/jwt-auth/oauth/google1").execute();
            Response r = Response.status(404)./*header("Access-Control-Allow-Origin",url)*/build();
            return r;
        }
        else
        {
            Response r = Response.status(404)./*header("Access-Control-Allow-Origin",url)*/build();
            return r;
        }


    }


    @GET
    @Path("google2")
    public Response google2() throws IOException, TokenResponseException {
        Response r = Response.status(404)./*header("Access-Control-Allow-Origin",url)*/build();
        return r;
    }
}
