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

package com.valtimo.keycloak.security.jwt.provider;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultClaims;
import io.jsonwebtoken.security.Keys;
import org.apache.commons.codec.binary.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.Key;
import java.security.KeyPair;
import java.util.List;

import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.USER;
import static com.ritense.valtimo.contract.security.jwt.JwtConstants.ROLES_SCOPE;
import static com.valtimo.keycloak.security.jwt.authentication.KeycloakTokenAuthenticator.REALM_ACCESS;
import static org.assertj.core.api.Assertions.assertThat;

public class KeycloakSecretKeyProviderTest {

    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.RS256;
    private KeycloakSecretKeyProvider keycloakSecretKeyProvider;
    private KeyPair keyPair;

    @BeforeEach
    public void setUp() {
        keyPair = Keys.keyPairFor(signatureAlgorithm);
        keycloakSecretKeyProvider = new KeycloakSecretKeyProvider(
            Base64.encodeBase64String(keyPair.getPublic().getEncoded())
        );
    }

    @Test
    public void secretWithBase64Encoding() {
        //given
        final int secretBase64Length = keyPair.getPublic().getEncoded().length;

        //when
        final Key key = keycloakSecretKeyProvider.getKey(signatureAlgorithm);

        //then
        assertThat(key).isNotNull();
        assertThat(key.getEncoded()).hasSize(secretBase64Length);
    }

    @Test
    public void supportsNoRoleClaim() {
        //given
        final Claims emptyClaim = new DefaultClaims();

        //when
        boolean supports = keycloakSecretKeyProvider.supports(signatureAlgorithm, emptyClaim);

        //then
        assertThat(supports).isFalse();
    }

    @Test
    public void supportsSuccessfull() {
        //given
        final Claims claims = claimsWithRealmAccessRoles();

        //when
        boolean supports = keycloakSecretKeyProvider.supports(signatureAlgorithm, claims);

        //then
        assertThat(supports).isTrue();
    }

    private Claims claimsWithRealmAccessRoles() {
        final Claims roles = new DefaultClaims();
        roles.put(ROLES_SCOPE, List.of(USER));
        return buildRealmClain(roles);
    }

    private Claims buildRealmClain(Claims role) {
        final Claims claims = new DefaultClaims();
        claims.put(REALM_ACCESS, role);
        return claims;
    }

}