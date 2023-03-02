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

package com.ritense.valtimo.security.jwt;

import com.ritense.tenancy.authentication.TenantAuthenticationToken;
import com.ritense.tenancy.authentication.TenantAware;
import com.ritense.valtimo.contract.config.ValtimoProperties;
import com.ritense.valtimo.security.jwt.authentication.TokenAuthenticationService;
import com.ritense.valtimo.security.jwt.exception.TokenAuthenticatorNotFoundException;
import io.jsonwebtoken.ExpiredJwtException;
import org.camunda.bpm.engine.IdentityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import static com.ritense.valtimo.contract.security.jwt.JwtConstants.AUTHORIZATION_HEADER;

/**
 * Filters incoming requests and installs a Spring Security principal if a header corresponding to a valid user is
 * found.
 */
public class JwtFilter extends GenericFilterBean {

    private final Logger slf4jLogger = LoggerFactory.getLogger(JwtFilter.class);
    private final IdentityService identityService;
    private final TokenAuthenticationService tokenAuthenticationService;

    private final ValtimoProperties valtimoProperties;

    public JwtFilter(
        IdentityService identityService,
        TokenAuthenticationService tokenAuthenticationService,
        ValtimoProperties valtimoProperties
    ) {
        this.identityService = identityService;
        this.tokenAuthenticationService = tokenAuthenticationService;
        this.valtimoProperties = valtimoProperties;
    }

    @Override
    public void doFilter(
        ServletRequest servletRequest,
        ServletResponse servletResponse,
        FilterChain filterChain
    ) throws IOException, ServletException {
        try {
            String authenticatedUserId = null;
            Authentication authentication = null;
            HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
            slf4jLogger.debug("Request URL '{}'", httpServletRequest.getRequestURL().toString());
            String jwt = resolveToken(httpServletRequest);
            if (StringUtils.hasText(jwt)) {
                if (this.tokenAuthenticationService.validateToken(jwt)) {
                    authentication = this.tokenAuthenticationService.getAuthentication(jwt);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    authenticatedUserId = authentication.getName();
                }
            }
            if (authentication != null) {
                if (valtimoProperties.getApp().getEnableTenancy()) {
                    if (authentication instanceof TenantAware) {
                        final var tenantAuthenticationToken = (TenantAuthenticationToken) authentication;
                        final String tenantId = tenantAuthenticationToken.getTenantId();
                        slf4jLogger.debug(
                            "Camunda multi-tenant setAuthenticatedUserId='{}' with tenantId='{}'",
                            authenticatedUserId,
                            tenantId
                        );
                        identityService.setAuthentication(authenticatedUserId, null, List.of(tenantId));
                    } else {
                        slf4jLogger.debug(
                            "Missing TenantAware authentication found instead '{}' skipping multi-tenancy",
                            authentication.getClass().getSimpleName()
                        );
                    }
                } else {
                    slf4jLogger.debug("Camunda setAuthenticatedUserId='{}'", authenticatedUserId);
                    identityService.setAuthenticatedUserId(authenticatedUserId);
                }
            }
            filterChain.doFilter(servletRequest, servletResponse);
            // User (authentication) should always be set or reset to null
            // because the camunda implementation will remember the user id from a previous request.
            identityService.clearAuthentication();
        } catch (ExpiredJwtException eje) {
            slf4jLogger.info("Security exception for user {} - {}", eje.getClaims().getSubject(), eje.getMessage());
            ((HttpServletResponse) servletResponse).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            filterChain.doFilter(servletRequest, servletResponse);
        } catch (TokenAuthenticatorNotFoundException e) {
            ((HttpServletResponse) servletResponse).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    private String resolveToken(HttpServletRequest request) {
        final String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

}
