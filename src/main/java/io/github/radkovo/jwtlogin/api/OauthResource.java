package io.github.radkovo.jwtlogin.api;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.api.client.auth.oauth2.*;
import com.google.api.client.auth.oauth2.TokenResponse;
import io.github.radkovo.jwtlogin.JwtTokenGenerator;
import io.github.radkovo.jwtlogin.OauthUtils;
import io.github.radkovo.jwtlogin.SupportedOauthServices;
import io.github.radkovo.jwtlogin.dao.LogService;
import io.github.radkovo.jwtlogin.dao.OauthAccessTokenService;
import io.github.radkovo.jwtlogin.dao.OauthUserService;
import io.github.radkovo.jwtlogin.dao.UserService;
import io.github.radkovo.jwtlogin.data.*;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.*;
import java.net.*;
import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;


/**
 * @author xondri05
 *
 */

@Path("oauth")
public class OauthResource {

    private String id;

    private static final long TOKEN_DURATION = 7200;

    @Inject
    OauthUtils oauthUtils;

    @Inject
    Principal principal;

    @Inject
    LogService logService;

    @Inject
    UserService userService;

    @Inject
    OauthUserService oauthUserService;

    @Inject
    OauthAccessTokenService oauthAccessTokenService;

    @Inject
    @ConfigProperty(name = "jwtauth.privatekey.location", defaultValue = "")
    String privateKeyLocation;

    @Inject
    @ConfigProperty(name = "frontend.base.url", defaultValue = "")
    private String frontendOauthUrl;

    @Inject
    @ConfigProperty(name = "backend.base.url", defaultValue = "")
    private String backendOauthUrl;



    @GET
    public String ping() {
        return "Ping OK";
    }

    @GET
    @Path("userInfo")
    @PermitAll
    public Response getUserInfo()
    {
        String email = (principal != null) ? principal.getName() : "unknown";
        OauthUser user = oauthUserService.getOauthUserByEmail(email).orElse(null);
        if (user != null)
            return Response.ok(new ResultResponse("ok", new OauthUserDTO(user))).build();
        else
            return Response.ok(new ResultResponse("unknown", null)).build();
    }


    ////////////////////////////////////////
    // GOOGLE OAUTH
    ///////////////////////////////////////

    @GET
    @Path("google-start")
    public Response google_start() throws IOException, URISyntaxException {
        URI url = new URI(oauthUtils.initializeFlowGoogle().newAuthorizationUrl().setState("xyz")
                .setRedirectUri(backendOauthUrl+"oauth/google-redirect").build());
        return Response.temporaryRedirect(url).build();
    }

    @GET
    @Path("google-redirect")
    public Response google_redirect(@QueryParam("oauthid") String oauthid ,@QueryParam("state") String state
            , @QueryParam("code") String code, @QueryParam("authuser") String authuser
            , @QueryParam("prompt") String prompt, @QueryParam("scope") String scope)
            throws IOException, TokenResponseException, URISyntaxException {

        if (code != null)
        {
            TokenResponse token = oauthUtils.initializeFlowGoogle().newTokenRequest(code)
                    .setRedirectUri(backendOauthUrl+"oauth/google-redirect").execute();
            OauthAccessToken token1 = oauthAccessTokenService.createOauthAccessToken(
                    new OauthAccessTokenDTO(token.getAccessToken()));

            URI url = new URI(frontendOauthUrl+"oauth"
                    +"?uuid="+token1.getId()+"&service="+SupportedOauthServices.GOOGLE);
            return Response.temporaryRedirect(url).build();
        }
        else
        {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new MessageResponse("Authorization code as query-param is missing"))
                    .build();
        }
    }

    @POST
    @Path("google-finish")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response google_finish(OauthCredentialsId oauthCredentialsId) throws IOException {
        if (oauthCredentialsId != null && oauthCredentialsId.getOauthCredetnialsId() != null){
            try
            {
                OauthAccessToken accessToken = oauthAccessTokenService.getOauthAccessTokenById(
                        oauthCredentialsId.getOauthCredetnialsId()).orElse(null);
                if(accessToken==null) {
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity(new MessageResponse("Access token not exists"))
                            .build();
                }
                oauthAccessTokenService.deleteOauthAccessToken(accessToken.getId());
                GoogleUserInfo googleUserInfo = oauthUtils.getGoogleUserInfo(accessToken);

                OauthUserDTO dto = new OauthUserDTO(SupportedOauthServices.GOOGLE
                        ,googleUserInfo.getId(),googleUserInfo.getEmail(),googleUserInfo.getName());

                OauthUser oauthUser = oauthUserService.getOauthUserByEmail(dto.getEmail()).orElse(null);
                OauthUser newOauthUser;

                if(oauthUser == null) { //first oauth login
                    newOauthUser = oauthUserService.createOauthUser(dto);
                } else { //not first oauth login
                    if(oauthUser.getOauthService().equals(SupportedOauthServices.GOOGLE)){
                        newOauthUser = oauthUserService.updateOauthUser(dto.getEmail(), dto);
                    }else{
                        logService.log(new LogEntry("auth", "login", dto.getEmail()
                                , "Invalid oauth login through Google"));
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity(new MessageResponse("Email account has already been logged in through the oauth service: "
                                        +oauthUser.getOauthService()))
                                .build();
                    }
                }

                String token = JwtTokenGenerator.generateJWTString(newOauthUser.getEmail(),newOauthUser.getEmail(),
                        TOKEN_DURATION, newOauthUser.getRoles(), privateKeyLocation);
                io.github.radkovo.jwtlogin.data.TokenResponse resp
                        = new io.github.radkovo.jwtlogin.data.TokenResponse(token);
                logService.log(new LogEntry("auth", "login", newOauthUser.getEmail()
                        , "Successfull login through oauth service Google"));

                return Response.ok(resp).build();
            } catch (Exception e) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).build();
            }
        }else{
            logService.log(new LogEntry("auth", "login", "unknown"
                    , "Invalid oauth login through Google"));
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new MessageResponse("Required oauth-credential ID is missing in request body"))
                    .build();
        }
    }



    ////////////////////////////////////////
    // GITHUB OAUTH
    ///////////////////////////////////////

    @GET
    @Path("github-start")
    public Response github_start() throws IOException, URISyntaxException {
        URI url = new URI(oauthUtils.initializeFlowGithub().newAuthorizationUrl().setState("xyz")
                .setRedirectUri(backendOauthUrl+"oauth/github-redirect").build());
        return Response.temporaryRedirect(url).build();
    }

    @GET
    @Path("github-redirect")
    public Response github_redirect(@QueryParam("oauthid") String oauthid ,@QueryParam("state") String state
            , @QueryParam("code") String code, @QueryParam("authuser") String authuser
            , @QueryParam("prompt") String prompt, @QueryParam("scope") String scope)
            throws IOException, TokenResponseException, URISyntaxException {

        if (code != null)
        {

            String accessToken = oauthUtils.obtainGithubAccessToken(code);

            OauthAccessToken token1 = oauthAccessTokenService.createOauthAccessToken(
                    new OauthAccessTokenDTO(accessToken));

            URI url = new URI(frontendOauthUrl+"oauth"
                    +"?uuid="+token1.getId()+"&service="+SupportedOauthServices.GITHUB);
            return Response.temporaryRedirect(url).build();
        }
        else
        {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new MessageResponse("Authorization code as query-param is missing"))
                    .build();
        }
    }

    @POST
    @Path("github-finish")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response github_finish(OauthCredentialsId oauthCredentialsId) throws IOException {
        if (oauthCredentialsId != null && oauthCredentialsId.getOauthCredetnialsId() != null){
            try
            {
                OauthAccessToken accessToken = oauthAccessTokenService.getOauthAccessTokenById(
                        oauthCredentialsId.getOauthCredetnialsId()).orElse(null);

                if(accessToken==null) {
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity(new MessageResponse("Access token not exists"))
                            .build();
                }
                oauthAccessTokenService.deleteOauthAccessToken(accessToken.getId());
                GithubUserInfo githubUserInfo = oauthUtils.getGithubUserInfo(accessToken);

                OauthUserDTO dto = new OauthUserDTO(SupportedOauthServices.GITHUB
                        ,githubUserInfo.getId(),githubUserInfo.getLogin(),githubUserInfo.getName());

                OauthUser oauthUser = oauthUserService.getOauthUserByEmail(dto.getEmail()).orElse(null);
                OauthUser newOauthUser;

                if(oauthUser == null) { //first oauth login
                    newOauthUser = oauthUserService.createOauthUser(dto);
                } else { //not first oauth login
                    if(oauthUser.getOauthService().equals(SupportedOauthServices.GITHUB)){
                        newOauthUser = oauthUserService.updateOauthUser(dto.getEmail(), dto);
                    }else{
                        logService.log(new LogEntry("auth", "login", dto.getEmail()
                                , "Invalid oauth login through Github"));
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity(new MessageResponse("Email account has already been logged in through the oauth service: "
                                    +oauthUser.getOauthService()))
                                .build();
                    }
                }

                String token = JwtTokenGenerator.generateJWTString(newOauthUser.getEmail(),newOauthUser.getEmail(),
                        TOKEN_DURATION, newOauthUser.getRoles(), privateKeyLocation);
                io.github.radkovo.jwtlogin.data.TokenResponse resp
                        = new io.github.radkovo.jwtlogin.data.TokenResponse(token);
                logService.log(new LogEntry("auth", "login", newOauthUser.getEmail()
                        , "Successfull login through oauth service Github"));

                return Response.ok(resp).build();
            } catch (Exception e) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).build();
            }
        }else{
            logService.log(new LogEntry("auth", "login", "unknown"
                    , "Invalid oauth login through Github"));
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new MessageResponse("Required oauth-credential ID is missing in request body"))
                    .build();
        }
    }
}
