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

package com.ritense.mail;

import com.ritense.mail.domain.blacklist.BlacklistedEmail;
import com.ritense.mail.repository.BlacklistRepository;
import com.ritense.mail.service.BlacklistService;
import com.ritense.valtimo.contract.basictype.EmailAddress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BlacklistServiceTest {

    private static final String EMAIL = "test@ritense.com";

    private BlacklistRepository blacklistRepository;
    private BlacklistService blacklistService;

    @BeforeEach
    public void setUp() {
        blacklistRepository = mock(BlacklistRepository.class);
        blacklistService = spy(new BlacklistService(blacklistRepository));
    }

  /*  @Test
    public void shouldBlacklistEmailAddress() {
        when(blacklistRepository.findByEmailAddress(anyString())).thenReturn(Optional.empty());

        blacklistService.blacklist(EmailAddress.from(EMAIL), "test reason");

        verify(blacklistRepository, times(1)).save(any(BlacklistedEmail.class));
    }

    @Test
    public void shouldNotBlacklistEmailAddressForDuplicateEmailAddress() {
        BlacklistedEmail emailFound = new BlacklistedEmail(EMAIL, "test");
        when(blacklistRepository.findByEmailAddress(anyString())).thenReturn(Optional.of(emailFound));

        blacklistService.blacklist(EmailAddress.from(EMAIL), "test reason2");

        verify(blacklistRepository, times(0)).save(any(BlacklistedEmail.class));
    }

    @Test
    public void isAlreadyBlacklistShouldReturnTrue() {
        BlacklistedEmail emailFound = new BlacklistedEmail(EMAIL, "test");
        when(blacklistRepository.findByEmailAddress(anyString())).thenReturn(Optional.of(emailFound));

        boolean blacklisted = blacklistService.isBlacklisted(EmailAddress.from(EMAIL));

        assertThat(blacklisted, is(true));
    }

    @Test
    public void isNotBlacklistShouldReturnFalse() {
        when(blacklistRepository.findByEmailAddress(anyString())).thenReturn(Optional.empty());

        boolean blacklisted = blacklistService.isBlacklisted(EmailAddress.from(EMAIL));

        assertThat(blacklisted, is(false));
    }*/

}