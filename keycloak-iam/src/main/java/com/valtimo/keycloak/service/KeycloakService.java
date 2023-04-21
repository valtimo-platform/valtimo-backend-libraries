/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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

public class KeycloakService {

    private final KeycloakSpringBootProperties properties;
    private final String clientName;

    public KeycloakService(KeycloakSpringBootProperties properties, String keycloakClientName) {
        this.properties = properties;
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

    public UsersResource usersResource() {
        return realmResource().users();
    }

    public RolesResource realmRolesResource() {
        return realmResource().roles();
    }

    public RolesResource clientRolesResource() {
        return clientResource().roles();
    }

    private RealmResource realmResource() {
        return keycloak().realm(properties.getRealm());
    }

    private ClientResource clientResource() {
        var clients = keycloak().realm(properties.getRealm()).clients().findByClientId(clientName);

        if (clients.size() == 1) {
            ClientResource clientResource = keycloak().realm(properties.getRealm()).clients().get(clients.get(0).getId());
            return clientResource;
        } else {
            throw new IllegalStateException("Expected exactly 1 client with name " + clientName + " but found: " + clients.size());
        }

    }

}
