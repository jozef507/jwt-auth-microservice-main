package io.github.radkovo.jwtlogin.dao;

import io.github.radkovo.jwtlogin.Roles;
import io.github.radkovo.jwtlogin.data.OauthUser;
import io.github.radkovo.jwtlogin.data.OauthUserDTO;
import io.github.radkovo.jwtlogin.data.User;
import io.github.radkovo.jwtlogin.data.UserDTO;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 *
 * @author xondri05
 */
public class OauthUserService {
    private static final Set<String> defaultRoles = Set.of(Roles.USER);

    @PersistenceContext(unitName = "usersPU")
    EntityManager em;

    @Transactional
    public OauthUser createOauthUser(OauthUserDTO dto)
    {
        OauthUser newUser = new OauthUser(dto.getOauthService(), dto.getServiceId(), dto.getEmail(), dto.getName());
        newUser.setRoles(defaultRoles);
        em.persist(newUser);
        em.flush();
        return newUser;
    }

    public List<OauthUser> getOauthUsers()
    {
        return em.createNamedQuery("OauthUser.all", OauthUser.class).getResultList();
    }

    public Optional<OauthUser> getOauthUserByEmail(String email)
    {
        return em.createNamedQuery("OauthUser.byEmail", OauthUser.class)
                .setParameter("email", email)
                .getResultList()
                .stream()
                .findFirst();
    }

    @Transactional
    public OauthUser updateOauthUser(String email, OauthUserDTO dto)
    {
        OauthUser user = getOauthUserByEmail(email).orElse(null);
        if (user != null)
        {
            user.updateWith(dto);
            em.merge(user);
            em.flush();
            return user;
        }
        else
            return null;
    }

    @Transactional
    public OauthUser deleteOauthUser(String email)
    {
        OauthUser user = getOauthUserByEmail(email).orElse(null);
        if (user != null)
        {
            em.remove(user);
            em.flush();
            return user;
        }
        else
            return null;
    }
}
