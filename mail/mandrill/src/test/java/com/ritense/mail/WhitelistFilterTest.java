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

import com.ritense.mail.config.MailingProperties;
import com.ritense.mail.domain.filters.WhitelistFilter;
import com.ritense.valtimo.contract.basictype.EmailAddress;
import com.ritense.valtimo.contract.mail.model.RawMailMessage;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WhitelistFilterTest {

    private MailingProperties mailingProperties = mock(MailingProperties.class);
    private WhitelistFilter whitelistFilter = new WhitelistFilter(mailingProperties);

    @Test
    public void shouldFilterOutRecipientNotOnWhitelist() {
        EmailAddress redirectEmailAddress = EmailAddress.from("test@ritense.com");
        RawMailMessage rawMailMessageTest = RawMailMessageHelper.createRawMailMessageTest(redirectEmailAddress.get());

        when(mailingProperties.getWhitelistedEmailAddresses()).thenReturn(emptyWhitelistCollection());

        whitelistFilter.doFilter(rawMailMessageTest);

        assertThat(false, is(rawMailMessageTest.recipients.isPresent()));
    }

    @Test
    public void shouldContainWhitelistRecipient() {
        EmailAddress redirectEmailAddress = EmailAddress.from("test@ritense.com");
        RawMailMessage rawMailMessageTest = RawMailMessageHelper.createRawMailMessageTest(redirectEmailAddress.get());

        when(mailingProperties.getWhitelistedEmailAddresses()).thenReturn(whitelistCollection());

        whitelistFilter.doFilter(rawMailMessageTest);

        assertThat(true, is(rawMailMessageTest.recipients.isPresent()));
    }

    @Test
    public void shouldContainWhitelistRecipientBasesOnDomain() {
        EmailAddress redirectEmailAddress = EmailAddress.from("test@ritense.com");
        RawMailMessage rawMailMessageTest = RawMailMessageHelper.createRawMailMessageTest(redirectEmailAddress.get());

        when(mailingProperties.getWhitelistedDomains()).thenReturn(whitelistDomains());

        whitelistFilter.doFilter(rawMailMessageTest);

        assertThat(true, is(rawMailMessageTest.recipients.isPresent()));
    }

    private List<String> whitelistDomains() {
        List<String> whitelist = new ArrayList<>();
        whitelist.add("ritense.com");
        return whitelist;
    }

    private List<String> emptyWhitelistCollection() {
        return new ArrayList<>();
    }

    private List<String> whitelistCollection() {
        List<String> whitelist = new ArrayList<>();
        whitelist.add("test@ritense.com");
        return whitelist;
    }

}