/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
 *
 * Licensed under EUPL, Version 1.2 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
