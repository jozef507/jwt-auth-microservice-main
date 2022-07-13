package io.github.radkovo.jwtlogin.data;

import javax.persistence.*;
import java.util.Set;
import io.github.radkovo.jwtlogin.data.OauthUserDTO;

/**
 * @author Jozef Ondria
 *
 */

@Entity
@Table(name = "oauthusers")
@NamedQueries({
        @NamedQuery(name = "OauthUser.all", query = "select ou from OauthUser ou order by ou.id"),
        @NamedQuery(name = "OauthUser.byEmail", query = "select ou from OauthUser ou where ou.email = :email")
})
public class OauthUser {

    @Id

    @GeneratedValue
    private long id;

    @Column(nullable = false, unique = true)
    private String email;

    private String oauthService;

    private String serviceId;

    private String name;

    @ElementCollection
    private Set<String> roles;


    public OauthUser()
    {
    }

    public OauthUser(String oauthService, String serviceId, String email, String name)
    {
        this.email = email;
        this.oauthService = oauthService;
        this.serviceId = serviceId;
        this.name = name;
        this.roles = Set.of();
    }

    public void updateWith(OauthUserDTO dto)
    {
        if (dto.getEmail() != null)
            setEmail(dto.getEmail());
        if (dto.getOauthService() != null)
            setOauthService(dto.getOauthService());
        if (dto.getServiceId() != null)
            setServiceId(dto.getServiceId());
        if (dto.getName() != null)
            setName(dto.getName());
        if (dto.getRoles() != null)
        {
            roles.clear();
            roles.addAll(dto.getRoles());
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getOauthService() {
        return oauthService;
    }

    public void setOauthService(String oauthService) {
        this.oauthService = oauthService;
    }
}

