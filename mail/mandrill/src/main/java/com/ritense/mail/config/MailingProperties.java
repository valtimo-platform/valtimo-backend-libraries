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

package com.ritense.mail.config;

import com.ritense.valtimo.contract.basictype.EmailAddress;
import com.ritense.valtimo.contract.basictype.SimpleName;
import com.ritense.valtimo.contract.mail.model.value.Recipient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@ConfigurationProperties(prefix = "mailing", ignoreUnknownFields = false)
public class MailingProperties {
    private boolean onlyAllowWhitelistedRecipients = false;

    /**
     * Filters with a higher priority will get executed later.
     */
    private int whitelistedPriority = -1;

    private List<String> whitelistedEmailAddresses = new ArrayList<>();
    private List<String> whitelistedDomains = new ArrayList<>();

    private boolean redirectAllMails = false;

    /**
     * Filters with a higher priority will get executed later.
     */
    private int redirectAllMailsPriority = -1;

    private Collection<Recipient> sendRedirectedMailsTo = new ArrayList<>();

    private int blacklistFilterPriority = 10;
    private boolean blacklistFilter = true;

    public boolean isOnlyAllowWhitelistedRecipients() {
        return onlyAllowWhitelistedRecipients;
    }

    public void setOnlyAllowWhitelistedRecipients(boolean onlyAllowWhitelistedRecipients) {
        this.onlyAllowWhitelistedRecipients = onlyAllowWhitelistedRecipients;
    }

    public boolean isRedirectAllMails() {
        return redirectAllMails;
    }

    public void setRedirectAllMails(boolean redirectAllMails) {
        this.redirectAllMails = redirectAllMails;
    }

    public List<String> getWhitelistedEmailAddresses() {
        return whitelistedEmailAddresses;
    }

    public void setWhitelistedEmailAddresses(List<String> whitelistedEmailAddresses) {
        this.whitelistedEmailAddresses = whitelistedEmailAddresses;
    }

    public List<String> getWhitelistedDomains() {
        return whitelistedDomains;
    }

    public void setWhitelistedDomains(List<String> whitelistedDomains) {
        this.whitelistedDomains = whitelistedDomains;
    }

    public Collection<Recipient> getSendRedirectedMailsTo() {
        return sendRedirectedMailsTo;
    }

    public void setSendRedirectedMailsTo(List<String> sendRedirectedMailsTo) {
        List<Recipient> redirectRecipients = sendRedirectedMailsTo.stream()
                .map(mailto -> Recipient.to(EmailAddress.from(mailto), SimpleName.none()))
                .collect(Collectors.toList());

        this.sendRedirectedMailsTo = redirectRecipients;
    }

    /**
     * The priority determines the order in which filters will get executed.
     * Filters with a higher priority will get executed later.
     *
     * @return the priority of the WhitelistFilter
     * @see com.ritense.valtimo.mail.filters.WhitelistFilter
     */
    public int getWhitelistedPriority() {
        return whitelistedPriority;
    }

    public void setWhitelistedPriority(int whitelistedPriority) {
        this.whitelistedPriority = whitelistedPriority;
    }

    /**
     * The priority determines the order in which filters will get executed.
     * Filters with a higher priority will get executed later.
     *
     * @return the priority of the RedirectToFilter
     * @see com.ritense.valtimo.mail.filters.RedirectToFilter
     */
    public int getRedirectAllMailsPriority() {
        return redirectAllMailsPriority;
    }

    public void setRedirectAllMailsPriority(int redirectAllMailsPriority) {
        this.redirectAllMailsPriority = redirectAllMailsPriority;
    }

    public int getBlacklistFilterPriority() {
        return blacklistFilterPriority;
    }

    public void setBlacklistFilterPriority(int blacklistFilterPriority) {
        this.blacklistFilterPriority = blacklistFilterPriority;
    }

    public boolean isBlacklistFilter() {
        return blacklistFilter;
    }

    public void setBlacklistFilter(boolean blacklistFilter) {
        this.blacklistFilter = blacklistFilter;
    }
}
