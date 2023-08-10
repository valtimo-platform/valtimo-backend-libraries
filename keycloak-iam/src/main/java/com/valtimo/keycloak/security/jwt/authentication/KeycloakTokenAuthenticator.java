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

package com.valtimo.keycloak.security.jwt.authentication;

import com.ritense.tenancy.authentication.TenantAuthenticationToken;
import com.ritense.valtimo.contract.config.ValtimoProperties;
import com.ritense.valtimo.contract.security.jwt.TokenAuthenticator;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.ritense.valtimo.contract.authentication.AuthoritiesConstants.USER;
import static com.ritense.valtimo.contract.security.jwt.JwtConstants.*;
import static java.util.Objects.requireNonNull;

public class KeycloakTokenAuthenticator extends TokenAuthenticator {

    private static final Logger logger = LoggerFactory.getLogger(KeycloakTokenAuthenticator.class);
    public final static String REALM_ACCESS = "realm_access";
    public final static String RESOURCE_ACCESS = "resource_access";
    private final ValtimoProperties valtimoProperties;
    private final String clientName;

    public KeycloakTokenAuthenticator(String keycloakClient, ValtimoProperties valtimoProperties) {
        this.clientName = keycloakClient;
        this.valtimoProperties = valtimoProperties;
    }

    @Override
    public boolean supports(Claims claims) {
        try {
            final String email = getEmail(claims);
            if (email == null) {
                logger.info("Support failed: email must be present");
                return false;
            }
            if (email.isBlank()) {
                logger.info("Support failed: email is blank");
                return false;
            }
            final List<String> roles = getRoles(claims);
            boolean hasUserRole = roles.contains(USER);
            if (!hasUserRole) {
                logger.info("Support failed: missing USER_ROLE");
                return false;
            }
            return true;
        } catch (Exception e) {
            logger.info("Support failed with exception", e);
            return false;
        }
    }

    @Override
    public Authentication authenticate(String jwt, Claims claims) {
        requireNonNull(jwt, "jwt must not be null");
        requireNonNull(claims, "claims must not be null");

        final String email = getEmail(claims);
        final List<String> roles = getRoles(claims);

        if (email != null && !roles.isEmpty()) {
            final Set<? extends GrantedAuthority> authorities = roles.stream()
                .map(authority -> new SimpleGrantedAuthority(authority.toUpperCase()))
                .collect(Collectors.toSet());

            final User principal = new User(email, "", authorities);
            final Authentication authentication = new UsernamePasswordAuthenticationToken(principal, jwt, authorities);
            if (valtimoProperties.getApp().getEnableTenancy()) {
                logger.debug("Creating tenant authentication token");
                return new TenantAuthenticationToken(authentication, getTenantId(claims), getName(claims));
            }
            return authentication;
        }
        return null;
    }

    private String getTenantId(Claims claims) {
        if (claims.containsKey(TENANT_KEY)) {
            return claims.get(TENANT_KEY, String.class);
        }
        throw new IllegalStateException("Missing tenant key in claims");
    }

    private String getName(Claims claims) {
        if (claims.containsKey(NAME_KEY)) {
            return claims.get(NAME_KEY, String.class);
        }
        throw new IllegalStateException("Missing name key in claims");
    }

    private String getEmail(Claims claims) {
        return claims.get(EMAIL_KEY, String.class);
    }

    private List<String> getRoles(Claims claims) {
        final Map<String, List<String>> realmSettings = claims.get(REALM_ACCESS, Map.class);
        final Map<String, Map<String, List<String>>> resourceSettings = claims.get(RESOURCE_ACCESS, Map.class);
        final List<String> roles = new ArrayList<>(realmSettings.get(ROLES_SCOPE));

        if (!clientName.isBlank() && resourceSettings != null && resourceSettings.containsKey(clientName)) {
            roles.addAll(resourceSettings.get(clientName).get(ROLES_SCOPE));
        }
        return roles;
    }

}
