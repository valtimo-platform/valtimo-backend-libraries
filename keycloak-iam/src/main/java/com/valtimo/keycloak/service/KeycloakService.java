package com.valtimo.keycloak.service;

import lombok.RequiredArgsConstructor;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.OAuth2Constants;
import org.keycloak.adapters.springboot.KeycloakSpringBootProperties;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UsersResource;

@RequiredArgsConstructor
public class KeycloakService {

    private final KeycloakSpringBootProperties properties;

    public Keycloak keycloak() {
        return KeycloakBuilder.builder()
            .serverUrl(properties.getAuthServerUrl())
            .realm(properties.getRealm())
            .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
            .clientId(properties.getResource())
            .clientSecret((String) properties.getCredentials().get("secret"))
            .resteasyClient(
                new ResteasyClientBuilder()
                    .connectionPoolSize(10).build())
            .build();
    }

    public UsersResource usersResource() {
        return realmResource().users();
    }

    public RolesResource rolesResource() {
        return realmResource().roles();
    }

    private RealmResource realmResource() {
        return keycloak().realm(properties.getRealm());
    }

}
