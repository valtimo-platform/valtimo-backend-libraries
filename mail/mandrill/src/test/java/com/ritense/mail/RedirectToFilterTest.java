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
import com.ritense.mail.domain.filters.RedirectToFilter;
import com.ritense.valtimo.contract.basictype.EmailAddress;
import com.ritense.valtimo.contract.basictype.SimpleName;
import com.ritense.valtimo.contract.mail.model.RawMailMessage;
import com.ritense.valtimo.contract.mail.model.value.Recipient;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RedirectToFilterTest {

    private MailingProperties mailingProperties = mock(MailingProperties.class);
    private RedirectToFilter redirectToFilter = new RedirectToFilter(mailingProperties);

    @Test
    public void shouldFilterRedirectedRecipient() {
        Recipient testRecipient = Recipient.to(EmailAddress.from("test@ritense.com"), SimpleName.from("test"));
        RawMailMessage rawMailMessageTest = RawMailMessageHelper.createRawMailMessageTest(testRecipient);

        when(mailingProperties.getSendRedirectedMailsTo()).thenReturn(redirectCollection(testRecipient));

        redirectToFilter.doFilter(rawMailMessageTest);

        assertThat(true, is(rawMailMessageTest.recipients.isPresent()));
    }

    @Test
    public void shouldNotContainRecipients() {
        Recipient testRecipient = Recipient.to(EmailAddress.from("test@ritense.com"), SimpleName.from("test"));
        RawMailMessage rawMailMessageTest = RawMailMessageHelper.createRawMailMessageTest(testRecipient);

        when(mailingProperties.getSendRedirectedMailsTo()).thenReturn(emptyRedirectCollection());

        redirectToFilter.doFilter(rawMailMessageTest);

        assertThat(false, is(rawMailMessageTest.recipients.isPresent()));
    }

    private Collection<Recipient> redirectCollection(Recipient recipient) {
        Collection<Recipient> redirectCollection = new ArrayList<>();
        redirectCollection.add(recipient);
        return redirectCollection;
    }

    private Collection<Recipient> emptyRedirectCollection() {
        return new ArrayList<>();
    }

}