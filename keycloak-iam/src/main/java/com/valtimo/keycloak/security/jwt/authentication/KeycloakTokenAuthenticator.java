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

import com.ritense.valtimo.contract.security.jwt.TokenAuthenticator;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.USER;
import static com.ritense.valtimo.contract.security.jwt.JwtConstants.EMAIL_KEY;
import static com.ritense.valtimo.contract.security.jwt.JwtConstants.ROLES_SCOPE;
import static java.util.Objects.requireNonNull;

@Slf4j
@RequiredArgsConstructor
public class KeycloakTokenAuthenticator extends TokenAuthenticator {

    public final static String REALM_ACCESS = "realm_access";

    @Override
    public boolean supports(Claims claims) {
        try {
            final String email = getEmail(claims);
            if (email.isBlank()) {
                logger.debug("Support failed: email is blank");
                return false;
            }
            final List<String> roles = getRoles(claims);
            boolean hasUserRole = roles.contains(USER);
            if (!hasUserRole) {
                logger.debug("Support failed: missing USER_ROLE");
                return false;
            }
            return true;
        } catch (Exception e) {
            logger.debug("Support failed with exception", e);
            return false;
        }
    }

    @Override
    public Authentication authenticate(String jwt, Claims claims) {
        requireNonNull(jwt, "jwt must not be null");
        requireNonNull(claims, "claims must not be null");

        final String email = getEmail(claims);
        final List<String> roles = getRoles(claims);

        if (email != null && roles != null && !roles.isEmpty()) {
            final Set<? extends GrantedAuthority> authorities = roles.stream()
                .map(authority -> new SimpleGrantedAuthority(authority.toUpperCase()))
                .collect(Collectors.toSet());

            final User principal = new User(email, "", authorities);
            return new UsernamePasswordAuthenticationToken(principal, jwt, authorities);
        }
        return null;
    }

    private String getEmail(Claims claims) {
        return claims.get(EMAIL_KEY, String.class);
    }

    private List<String> getRoles(Claims claims) {
        final Map<String, List<String>> realmSettings = claims.get(REALM_ACCESS, Map.class);
        return realmSettings.get(ROLES_SCOPE);
    }

}