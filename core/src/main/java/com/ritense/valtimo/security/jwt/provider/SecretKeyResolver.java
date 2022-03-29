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

package com.ritense.valtimo.security.jwt.provider;

import com.ritense.valtimo.contract.security.jwt.provider.SecretKeyProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SigningKeyResolver;
import io.jsonwebtoken.lang.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.security.Key;
import java.util.List;

public class SecretKeyResolver implements SigningKeyResolver {

    private final List<SecretKeyProvider> secretKeyProviders;

    public SecretKeyResolver(List<SecretKeyProvider> secretKeyProviders) {
        this.secretKeyProviders = secretKeyProviders;
    }

    @Override
    public Key resolveSigningKey(JwsHeader header, Claims claims) {
        final SignatureAlgorithm algorithm = getSignatureAlgorithm(header);
        return secretKeyProviders
            .stream()
            .filter(provider -> provider.supports(algorithm, claims))
            .findFirst()
            .map(secretKeyProvider -> secretKeyProvider.getKey(algorithm))
            .orElse(null);
    }

    @Override
    public Key resolveSigningKey(JwsHeader header, String plaintext) {
        throw new UnsupportedOperationException();
    }

    private SignatureAlgorithm getSignatureAlgorithm(JwsHeader header) {
        SignatureAlgorithm algorithm = null;
        if (header != null) {
            String alg = header.getAlgorithm();
            if (Strings.hasText(alg)) {
                algorithm = SignatureAlgorithm.forName(alg);
            }
        }
        if (algorithm == null || algorithm == SignatureAlgorithm.NONE) {
            String msg = "JWT string has a digest/signature, but the header does not reference a valid signature " +
                "algorithm.";
            throw new MalformedJwtException(msg);
        }
        return algorithm;
    }

}