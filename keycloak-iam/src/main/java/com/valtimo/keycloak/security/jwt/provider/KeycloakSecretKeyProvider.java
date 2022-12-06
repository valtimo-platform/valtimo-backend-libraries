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

import com.ritense.valtimo.contract.security.jwt.provider.SecretKeyProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;

import static com.valtimo.keycloak.security.jwt.authentication.KeycloakTokenAuthenticator.REALM_ACCESS;
import static com.valtimo.keycloak.security.jwt.authentication.KeycloakTokenAuthenticator.RESOURCE_ACCESS;

public class KeycloakSecretKeyProvider implements SecretKeyProvider {

    private static final Logger logger = LoggerFactory.getLogger(KeycloakSecretKeyProvider.class);
    private final String secret;

    public KeycloakSecretKeyProvider(String secret) {
        this.secret = secret;
    }

    @Override
    public boolean supports(SignatureAlgorithm algorithm, Claims claims) {
        return algorithm.isRsa() && (claims.containsKey(REALM_ACCESS) || claims.containsKey(RESOURCE_ACCESS));
    }

    @Override
    public Key getKey(SignatureAlgorithm algorithm) {
        try {
            return getPublicKey(algorithm, getSecret());
        } catch (GeneralSecurityException e) {
            logger.error("Error resolving signing key", e);
        }
        return null;
    }

    private byte[] getSecret() {
        return Base64.decodeBase64(secret);
    }

    private RSAPublicKey getPublicKey(SignatureAlgorithm algorithm, byte[] key) throws GeneralSecurityException {
        final KeyFactory keyFactory = KeyFactory.getInstance(algorithm.getFamilyName());
        return (RSAPublicKey) keyFactory.generatePublic(new X509EncodedKeySpec(key));
    }

}
