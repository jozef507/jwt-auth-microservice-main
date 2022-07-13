package io.github.radkovo.jwtlogin.data;

import javax.persistence.*;
import java.util.Set;

/**
 * @author Jozef Ondria
 *
 */

@Entity
@Table(name = "oauthaccesstokens")
@NamedQueries({
        @NamedQuery(name = "OauthAccessToken.all", query = "select ou from OauthAccessToken ou order by ou.id"),
        @NamedQuery(name = "OauthAccessToken.byId", query = "select ou from OauthAccessToken ou where ou.id = :id")
})
public class OauthAccessToken {
    @Id
    @GeneratedValue
    private long id;

    @Column(nullable = false)
    private String accessToken;

    public OauthAccessToken()
    {
    }

    public OauthAccessToken(String accessToken)
    {
        this.accessToken = accessToken;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
