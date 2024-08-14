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

package com.valtimo.keycloak.service;

import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.keycloak.OAuth2Constants;
import org.keycloak.adapters.springboot.KeycloakSpringBootProperties;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UsersResource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static com.ritense.valtimo.contract.security.jwt.JwtConstants.EMAIL_KEY;
import static com.ritense.valtimo.contract.security.jwt.JwtConstants.ROLES_SCOPE;
import static com.valtimo.keycloak.security.jwt.authentication.KeycloakTokenAuthenticator.REALM_ACCESS;
import static com.valtimo.keycloak.security.jwt.authentication.KeycloakTokenAuthenticator.RESOURCE_ACCESS;
import com.valtimo.keycloak.security.config.ValtimoKeycloakPropertyResolver;

public class KeycloakService {

    public static final String KEYCLOAK_API_CLIENT_REGISTRATION = "keycloakapi";
    public static final String KEYCLOAK_JWT_CLIENT_REGISTRATION = "keycloakjwt";
    private final KeycloakSpringBootProperties properties;
    private final String clientName;

    public KeycloakService(KeycloakSpringBootProperties properties, String keycloakClientName) {
        this.properties = ValtimoKeycloakPropertyResolver.resolveProperties();
        this.clientName = keycloakClientName;
    }

    public Keycloak keycloak() {
        return KeycloakBuilder.builder()
            .serverUrl(properties.getAuthServerUrl())
            .realm(properties.getRealm())
            .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
            .clientId(properties.getResource())
            .clientSecret((String) properties.getCredentials().get("secret"))
            .resteasyClient(
                new ResteasyClientBuilderImpl()
                    .connectionPoolSize(10).build())
            .build();
    }

    public UsersResource usersResource(Keycloak keycloak) {
        return realmResource(keycloak).users();
    }

    public RolesResource realmRolesResource(Keycloak keycloak) {
        return realmResource(keycloak).roles();
    }

    public RolesResource clientRolesResource(Keycloak keycloak) {
        return clientResource(keycloak).roles();
    }

    public String getClientId(Keycloak keycloak) {
        if (clientName.isBlank()) {
            throw new IllegalStateException(
                "Error. Missing property: 'spring.security.oauth2.client.registration.keycloakjwt.client-id' or 'valtimo.keycloak.client'");
        }
        var clients = keycloak.realm(properties.getRealm()).clients().findByClientId(clientName);
        if (clients.size() == 1) {
            return clients.get(0).getId();
        } else {
            throw new IllegalStateException("Expected exactly 1 client with name " + clientName + " but found: " + clients.size());
        }
    }

    public RealmResource realmResource(Keycloak keycloak) {
        return keycloak.realm(properties.getRealm());
    }

    public String getEmail(Map<String, Object> claims) {
        return (String) claims.get(EMAIL_KEY);
    }

    public List<String> getRoles(Map<String, Object> claims) {
        final var realmSettings = (Map<String, List<String>>) claims.get(REALM_ACCESS);
        final var resourceSettings = (Map<String, Map<String, List<String>>>) claims.get(RESOURCE_ACCESS) ;

        final var roles = new ArrayList<>(realmSettings.get(ROLES_SCOPE));

        if (clientName != null && !clientName.isBlank() && resourceSettings != null && resourceSettings.containsKey(clientName)) {
            roles.addAll(resourceSettings.get(clientName).get(ROLES_SCOPE));
        }

        return roles;
    }

    private ClientResource clientResource(Keycloak keycloak) {
        return keycloak.realm(properties.getRealm()).clients().get(getClientId(keycloak));
    }

}
