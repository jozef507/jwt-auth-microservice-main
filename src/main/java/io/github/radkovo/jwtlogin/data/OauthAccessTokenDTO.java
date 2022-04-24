package io.github.radkovo.jwtlogin.data;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

/**
 *
 * @author xondri05
 */
public class OauthAccessTokenDTO {

    private String accessToken;

    public OauthAccessTokenDTO(OauthAccessToken oauthAccessToken)
    {
        accessToken = oauthAccessToken.getAccessToken();
    }

    public OauthAccessTokenDTO(String accessToken)
    {
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
