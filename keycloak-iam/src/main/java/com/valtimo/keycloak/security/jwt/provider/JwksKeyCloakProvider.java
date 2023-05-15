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

package com.valtimo.keycloak.security.jwt.provider;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.ritense.valtimo.contract.security.jwt.provider.SecretKeyProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.security.Key;
import static com.valtimo.keycloak.security.jwt.authentication.KeycloakTokenAuthenticator.REALM_ACCESS;
import static com.valtimo.keycloak.security.jwt.authentication.KeycloakTokenAuthenticator.RESOURCE_ACCESS;

public class JwksKeyCloakProvider implements SecretKeyProvider {

    private Logger logger = LoggerFactory.getLogger(JwksKeyCloakProvider.class);

    private final JWKSet jwkSet;

    public JwksKeyCloakProvider(JWKSet jwkSet) {
        this.jwkSet = jwkSet;
    }


    @Override
    public boolean supports(SignatureAlgorithm algorithm, Claims claims) {
        return algorithm.isRsa() && (claims.containsKey(REALM_ACCESS) || claims.containsKey(RESOURCE_ACCESS));
    }

    @Override
    public Key getKey(SignatureAlgorithm algorithm, String kid) {
        RSAKey rsaKey = this.jwkSet.getKeyByKeyId(kid).toRSAKey();
        if (rsaKey != null) {
            try {
                return rsaKey.toRSAPublicKey();
            } catch (JOSEException e) {
                logger.error(String.format("cannot get key for keyId %s", kid));
                throw new RuntimeException(String.format("error in retrieving public key for given keyId %s", kid), e);
            }
        }
        throw new IllegalStateException("Not able to return a key");
    }
}
