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

package com.ritense.valtimo.security.jwt.authentication;

import com.ritense.valtimo.contract.security.jwt.TokenAuthenticator;
import com.ritense.valtimo.security.jwt.provider.SecretKeyResolver;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class TokenAuthenticationService {

    private final List<TokenAuthenticator> tokenAuthenticators;
    private final SecretKeyResolver secretKeyResolver;

    public Authentication getAuthentication(String jwt) {
        final Claims claims = getClaims(jwt);
        return tokenAuthenticators
            .stream()
            .filter(tokenAuthenticator -> tokenAuthenticator.supports(claims))
            .findFirst()
            .map(tokenAuthenticator -> tokenAuthenticator.authenticate(jwt, claims))
            .orElse(null);
    }

    public boolean validateToken(final String jwt) {
        try {
            jwtSignedParser().parse(jwt);
            return true;
        } catch (SecurityException e) {
            logger.warn("Invalid JWT signature: " + e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid token.", e);
            return false;
        }
    }

    private Claims getClaims(final String jwt) {
        return jwtSignedParser().parseClaimsJws(jwt).getBody();
    }

    private JwtParser jwtSignedParser() {
        return Jwts.parserBuilder()
            .setSigningKeyResolver(secretKeyResolver)
            .build();
    }

}