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

package com.ritense.mail.domain.filters;

import com.ritense.mail.config.MailingProperties;
import com.ritense.mail.service.BlacklistService;
import com.ritense.valtimo.contract.mail.MailFilter;
import com.ritense.valtimo.contract.mail.model.RawMailMessage;
import com.ritense.valtimo.contract.mail.model.TemplatedMailMessage;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class BlacklistFilter implements MailFilter {

    private final BlacklistService blacklistService;
    private final MailingProperties mailingProperties;

    @Override
    public Optional<RawMailMessage> doFilter(RawMailMessage rawMailMessage) {
        rawMailMessage.recipients.filterBy(recipient -> !blacklistService.isBlacklisted(recipient.email));
        return Optional.of(rawMailMessage);
    }

    @Override
    public Optional<TemplatedMailMessage> doFilter(TemplatedMailMessage templatedMailMessage) {
        templatedMailMessage.recipients.filterBy(recipient -> !blacklistService.isBlacklisted(recipient.email));
        return Optional.of(templatedMailMessage);
    }

    @Override
    public boolean isEnabled() {
        return mailingProperties.isBlacklistFilter();
    }

    @Override
    public int getPriority() {
        return mailingProperties.getBlacklistFilterPriority();
    }

}