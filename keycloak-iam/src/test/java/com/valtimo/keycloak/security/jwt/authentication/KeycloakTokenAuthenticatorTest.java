/*
 * Copyright 2015-2020 Ritense BV, the Netherlands.
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

package com.valtimo.keycloak.security.jwt.authentication;

import com.ritense.valtimo.security.jwt.authentication.TokenAuthenticationService;
import com.ritense.valtimo.security.jwt.provider.SecretKeyResolver;
import com.valtimo.keycloak.security.jwt.provider.KeycloakSecretKeyProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultClaims;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.security.KeyPair;
import java.util.List;

import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.USER;
import static com.ritense.valtimo.contract.security.jwt.JwtConstants.NAME_KEY;
import static com.ritense.valtimo.contract.security.jwt.JwtConstants.ROLES_SCOPE;
import static com.valtimo.keycloak.security.jwt.authentication.KeycloakTokenAuthenticator.REALM_ACCESS;
import static org.apache.commons.codec.binary.Base64.encodeBase64String;
import static org.assertj.core.api.Assertions.assertThat;

public class KeycloakTokenAuthenticatorTest {

    private KeycloakSecretKeyProvider keycloakSecretKeyProvider;
    private SecretKeyResolver secretKeyResolver;
    private KeycloakTokenAuthenticator keycloakTokenAuthenticator;
    private TokenAuthenticationService tokenAuthenticationService;
    private KeyPair keyPair;

    @BeforeEach
    public void before() {
        keyPair = Keys.keyPairFor(SignatureAlgorithm.RS256);
        keycloakTokenAuthenticator = new KeycloakTokenAuthenticator();
        keycloakSecretKeyProvider = new KeycloakSecretKeyProvider(encodeBase64String(keyPair.getPublic().getEncoded()));
        secretKeyResolver = new SecretKeyResolver(List.of(keycloakSecretKeyProvider));
        tokenAuthenticationService = new TokenAuthenticationService(
            List.of(keycloakTokenAuthenticator),
            secretKeyResolver
        );
    }

    @Test
    public void shouldNotReturnAuthenticationWithUnknownRoleInToken() {
        String jwt = Jwts.builder()
            .setClaims(claimsWithUnknownRealmAccessRoles())
            .signWith(keyPair.getPrivate())
            .compact();

        Authentication authentication = tokenAuthenticationService.getAuthentication(jwt);

        assertThat(authentication).isNull();
    }

    @Test
    public void shouldReturnAuthenticationForMatchingRoleUser() {
        String jwt = Jwts.builder()
            .setClaims(claimsWithRealmAccessRoles())
            .signWith(keyPair.getPrivate())
            .compact();

        Authentication authentication = tokenAuthenticationService.getAuthentication(jwt);

        assertThat(authentication).isNotNull();
        assertThat(authentication).isInstanceOf(UsernamePasswordAuthenticationToken.class);
    }

    private Claims claimsWithRealmAccessRoles() {
        final Claims roles = new DefaultClaims();
        roles.put(ROLES_SCOPE, List.of(USER));
        return defaultKeycloakClaimWith(roles);
    }

    private Claims claimsWithUnknownRealmAccessRoles() {
        final Claims roles = new DefaultClaims();
        roles.put(ROLES_SCOPE, List.of("unknown-role"));
        return defaultKeycloakClaimWith(roles);
    }

    private Claims defaultKeycloakClaimWith(Claims role) {
        final Claims claims = new DefaultClaims();
        claims.put(REALM_ACCESS, role);
        claims.put(NAME_KEY, "userName");
        return claims;
    }

}