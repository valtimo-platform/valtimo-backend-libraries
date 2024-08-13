package org.keycloak.adapters.springboot;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "keycloak")
public class KeycloakSpringBootProperties {

    @JsonProperty("realm")
    private String realm;

    @JsonProperty("auth-server-url")
    private String authServerUrl;

    @JsonProperty("resource")
    private String resource;

    @JsonProperty("credentials")
    private Map<String, Object> credentials;

    public KeycloakSpringBootProperties(
        String realm,
        String authServerUrl,
        String resource,
        Map<String, Object> credentials
    ) {
        this.realm = realm;
        this.authServerUrl = authServerUrl;
        this.resource = resource;
        this.credentials = credentials;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public String getAuthServerUrl() {
        return authServerUrl;
    }

    public void setAuthServerUrl(String authServerUrl) {
        this.authServerUrl = authServerUrl;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public Map<String, Object> getCredentials() {
        return credentials;
    }

    public void setCredentials(Map<String, Object> credentials) {
        this.credentials = credentials;
    }
}
