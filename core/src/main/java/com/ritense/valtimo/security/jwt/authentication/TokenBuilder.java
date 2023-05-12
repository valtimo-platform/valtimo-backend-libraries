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

package com.ritense.valtimo.security.jwt.authentication;

import com.ritense.valtimo.contract.config.ValtimoProperties;
import com.ritense.valtimo.contract.security.jwt.provider.SecretKeyProvider;
import com.ritense.valtimo.security.jwt.provider.SecretKeyResolver;
import com.ritense.valtimo.security.jwt.token.TokenClaims;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.security.Key;
import java.util.Date;
import java.util.List;

@Deprecated(since = "Will be removed in the next major version")
public class TokenBuilder {

    private final List<SecretKeyProvider> secretKeyProviders;
    private final long tokenValidityInMilliSeconds;
    private final SecretKeyResolver secretKeyResolver;

    protected TokenBuilder(
        final ValtimoProperties valtimoProperties,
        final List<SecretKeyProvider> secretKeyProviders,
        final SecretKeyResolver secretKeyResolver
    ) {
        this.tokenValidityInMilliSeconds = 1000 * valtimoProperties.getJwt().getTokenValidityInSeconds();
        this.secretKeyProviders = secretKeyProviders;
        this.secretKeyResolver = secretKeyResolver;
    }

    public String createToken(SignatureAlgorithm algorithm, TokenClaims tokenClaims) {
        final Claims claims = tokenClaims.getClaims();
        final Key key = getKey(algorithm, claims);
        return Jwts.builder()
            .addClaims(claims)
            .signWith(key)
            .setExpiration(getValidity())
            .compact();
    }

    protected Date getValidity() {
        long now = (new Date()).getTime();
        return new Date(now + tokenValidityInMilliSeconds);
    }

    protected Key getKey(SignatureAlgorithm algorithm, Claims claims) {
        return secretKeyProviders.stream()
            .filter(secretKeyProvider -> secretKeyProvider.supports(algorithm, claims))
            .findFirst()
            .map(secretKeyProvider -> secretKeyProvider.getKey(algorithm, "aKeyId"))
            .orElse(null);
    }

    protected Claims getClaims(String token) {
        return Jwts.parserBuilder()
            .setSigningKeyResolver(secretKeyResolver)
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

}
