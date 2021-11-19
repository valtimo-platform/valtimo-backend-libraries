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

import com.ritense.mail.domain.filters.BlacklistFilter;
import com.ritense.mail.service.BlacklistService;
import com.ritense.valtimo.contract.basictype.EmailAddress;
import com.ritense.valtimo.contract.mail.model.RawMailMessage;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BlacklistFilterTest {

    private BlacklistService blacklistService = mock(BlacklistService.class);
    private BlacklistFilter blacklistFilter = new BlacklistFilter(blacklistService, null);

    @Test
    public void shouldFilterOutBlacklistedRecipient() {
        EmailAddress blacklistedEmail = EmailAddress.from("test@ritense.com");
        RawMailMessage rawMailMessageTest = RawMailMessageHelper.createRawMailMessageTest(blacklistedEmail.get());

        when(blacklistService.isBlacklisted(any(EmailAddress.class))).thenReturn(true);

        Optional<RawMailMessage> rawFilteredMailMessage = blacklistFilter.doFilter(rawMailMessageTest);

        assertThat(false, is(rawFilteredMailMessage.get().recipients.isPresent()));
    }

    @Test
    public void shouldNotFilterOutRecipient() {
        EmailAddress nonBlacklistedEmail = EmailAddress.from("test@ritense.com");
        RawMailMessage rawMailMessageTest = RawMailMessageHelper.createRawMailMessageTest(nonBlacklistedEmail.get());

        when(blacklistService.isBlacklisted(any(EmailAddress.class))).thenReturn(false);

        Optional<RawMailMessage> rawFilteredMailMessage = blacklistFilter.doFilter(rawMailMessageTest);

        assertThat(true, is(rawFilteredMailMessage.get().recipients.isPresent()));
    }

}