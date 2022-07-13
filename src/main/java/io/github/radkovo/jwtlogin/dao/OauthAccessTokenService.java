package io.github.radkovo.jwtlogin.dao;

import io.github.radkovo.jwtlogin.data.OauthAccessToken;
import io.github.radkovo.jwtlogin.data.OauthAccessTokenDTO;
import io.github.radkovo.jwtlogin.data.OauthUser;
import io.github.radkovo.jwtlogin.data.OauthUserDTO;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
/**
 * @author Jozef Ondria
 *
 */

public class OauthAccessTokenService {

    @PersistenceContext(unitName = "usersPU")
    EntityManager em;

    @Transactional
    public OauthAccessToken createOauthAccessToken(OauthAccessTokenDTO dto)
    {
        OauthAccessToken token = new OauthAccessToken(dto.getAccessToken());
        em.persist(token);
        em.flush();
        return token;
    }

    public List<OauthUser> getOauthAccessTokens()
    {
        return em.createNamedQuery("OauthAccessToken.all", OauthUser.class).getResultList();
    }

    public Optional<OauthAccessToken> getOauthAccessTokenById(String id)
    {
        return em.createNamedQuery("OauthAccessToken.byId", OauthAccessToken.class)
                .setParameter("id", Long.parseLong(id))
                .getResultList()
                .stream()
                .findFirst();
    }

    @Transactional
    public OauthAccessToken deleteOauthAccessToken(long id)
    {
        OauthAccessToken token = getOauthAccessTokenById(Long.toString(id)).orElse(null);
        if (token != null)
        {
            em.remove(token);
            em.flush();
            return token;
        }
        else
            return null;
    }


}
