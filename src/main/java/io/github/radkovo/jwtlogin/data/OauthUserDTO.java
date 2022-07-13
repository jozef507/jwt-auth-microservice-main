package io.github.radkovo.jwtlogin.data;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Set;
/**
 * @author Jozef Ondria
 *
 */

public class OauthUserDTO {

    private String oauthService;

    private String serviceId;

    private String email;

    private String name;

    private Set<String> roles;

    public OauthUserDTO()
    {
    }

    public OauthUserDTO(OauthUser oauthUser)
    {
        oauthService = oauthUser.getOauthService();
        serviceId = oauthUser.getServiceId();
        email = oauthUser.getEmail();
        name = oauthUser.getName();
        roles = oauthUser.getRoles();
    }

    public OauthUserDTO(String oauthService,String serviceId,String email,String name)
    {
        this.oauthService = oauthService;
        this.serviceId = serviceId;
        this.email = email;
        this.name = name;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
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

    public String getOauthService() {
        return oauthService;
    }

    public void setOauthService(String oauthService) {
        this.oauthService = oauthService;
    }
}
