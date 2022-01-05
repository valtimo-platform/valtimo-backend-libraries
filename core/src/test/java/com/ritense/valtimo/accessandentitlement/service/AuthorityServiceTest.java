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

package com.ritense.valtimo.accessandentitlement.service;

import com.ritense.valtimo.accessandentitlement.domain.Authority;
import com.ritense.valtimo.accessandentitlement.domain.AuthorityRequest;
import com.ritense.valtimo.accessandentitlement.domain.Money;
import com.ritense.valtimo.accessandentitlement.repository.AuthorityRepository;
import com.ritense.valtimo.accessandentitlement.service.impl.AuthorityServiceImpl;
import com.ritense.valtimo.service.AuthorizedUsersService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthorityServiceTest {

    private AuthorityRepository authorityRepository;
    private AuthorizedUsersService authorizedUsersService;
    private AuthorityService authorityService;

    @BeforeEach
    public void setUp() {
        authorityRepository = mock(AuthorityRepository.class);
        authorizedUsersService = mock(AuthorizedUsersService.class);
        authorityService = new AuthorityServiceImpl(authorityRepository, authorizedUsersService);
    }

    @Test
    void shouldCreateAndReturnAuthority() {
        when(authorityRepository.findById(any())).thenReturn(Optional.empty());

        final AuthorityRequest authorityRequest = new AuthorityRequest("role_name", BigDecimal.TEN);
        final Authority authority = authorityService.createAuthority(authorityRequest);

        assertThat(authority.getName()).isEqualTo(authorityRequest.getName());
        assertThat(authority.getSystemAuthority()).isEqualTo(false);
        assertThat(authority.getHourlyRate()).isEqualTo(new Money(authorityRequest.getHourlyRate()));
    }

    @Test
    void shouldUpdateAndReturnAuthority() {
        Authority authority = new Authority("role_name", BigDecimal.TEN, false);
        when(authorityRepository.findById(any())).thenReturn(Optional.of(authority));

        final AuthorityRequest authorityRequest = new AuthorityRequest("role_b", BigDecimal.ONE);
        authority = authorityService.updateAuthority(authorityRequest);

        assertThat(authority.getName()).isEqualTo(authorityRequest.getName());
        assertThat(authority.getSystemAuthority()).isEqualTo(false);
        assertThat(authority.getHourlyRate()).isEqualTo(new Money(authorityRequest.getHourlyRate()));
    }

    @Test
    void shouldDeleteAuthority() throws IllegalAccessException {
        Authority authority = new Authority("role_name", BigDecimal.TEN, false);
        when(authorityRepository.findById(any())).thenReturn(Optional.of(authority));
        when(authorizedUsersService.isRoleInUse(any())).thenReturn(false);

        authorityService.deleteAuthority(authority.getName());

        verify(authorityRepository).delete(authority);
    }

}