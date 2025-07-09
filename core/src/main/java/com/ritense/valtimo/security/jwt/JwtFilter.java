/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

import static com.ritense.valtimo.contract.security.jwt.JwtConstants.AUTHORIZATION_HEADER;

import com.ritense.valtimo.security.jwt.authentication.TokenAuthenticationService;
import com.ritense.valtimo.security.jwt.exception.TokenAuthenticatorNotFoundException;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.operaton.bpm.engine.IdentityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

/**
 * Filters incoming requests and installs a Spring Security principal if a header corresponding to a valid user is
 * found.
 */
public class JwtFilter extends GenericFilterBean {

    private final Logger slf4jLogger = LoggerFactory.getLogger(JwtFilter.class);
    private final IdentityService identityService;
    private final TokenAuthenticationService tokenAuthenticationService;

    public JwtFilter(IdentityService identityService, TokenAuthenticationService tokenAuthenticationService) {
        this.identityService = identityService;
        this.tokenAuthenticationService = tokenAuthenticationService;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        try {
            String authenticatedUserId = null;
            HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
            String jwt = resolveToken(httpServletRequest);
            if (StringUtils.hasText(jwt)) {
                if (this.tokenAuthenticationService.validateToken(jwt)) {
                    Authentication authentication = this.tokenAuthenticationService.getAuthentication(jwt);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    authenticatedUserId = authentication.getName();
                }
            }
            // user id should always be set or reset to null because the operaton implementation will remember the user id from a previous request.
            identityService.setAuthenticatedUserId(authenticatedUserId);
            filterChain.doFilter(servletRequest, servletResponse);
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
