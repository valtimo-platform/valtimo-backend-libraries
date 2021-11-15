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

package com.ritense.mail.service;

import com.microtripit.mandrillapp.lutung.MandrillApi;
import com.microtripit.mandrillapp.lutung.model.MandrillApiError;
import com.microtripit.mandrillapp.lutung.view.MandrillMessage;
import com.microtripit.mandrillapp.lutung.view.MandrillMessageStatus;
import com.ritense.mail.config.MandrillProperties;
import com.ritense.valtimo.contract.basictype.EmailAddress;
import com.ritense.valtimo.contract.mail.MailFilter;
import com.ritense.valtimo.contract.mail.MailSender;
import com.ritense.valtimo.contract.mail.model.MailMessageStatus;
import com.ritense.valtimo.contract.mail.model.RawMailMessage;
import com.ritense.valtimo.contract.mail.model.TemplatedMailMessage;
import com.ritense.valtimo.contract.mail.model.value.Recipient;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

@Slf4j
public class MandrillMailSender implements MailSender {

    //see: https://mandrill.zendesk.com/hc/en-us/articles/205582407-Does-Mandrill-Support-Attachments-
    private static final int MAX_SIZE_ATTACHMENTS = 16250000;

    private final DateFormat dateFormat;
    private final MandrillProperties mandrillProperties;
    private final MailMessageConverter mailMessageConverter;
    private final Collection<MailFilter> mailFilters;

    public MandrillMailSender(
        MandrillProperties mandrillProperties,
        MailMessageConverter mailMessageConverter,
        Collection<MailFilter> mailFilters
    ) {
        this.mandrillProperties = mandrillProperties;
        this.mailMessageConverter = mailMessageConverter;
        this.dateFormat = new SimpleDateFormat(mandrillProperties.getDateFormat());
        this.mailFilters = mailFilters;
    }

    @Override
    public Optional<List<MailMessageStatus>> send(RawMailMessage rawMailMessage) {
        Optional<RawMailMessage> optionalMailMessage = applyFilters(rawMailMessage);
        if (optionalMailMessage.isEmpty()) {
            logger.info("Mail message could not be sent; one or more filters caused the rawMailMessage to be invalid");
            return Optional.empty();
        }

        var mailMessage = optionalMailMessage.get();
        var mandrillMessage = mailMessageConverter.convert(mailMessage);
        var mandrillApi = getMandrillApi(rawMailMessage.isTest);
        Optional<List<MailMessageStatus>> mailStatus = Optional.empty();

        try {
            var mandrillMessageStatuses = mandrillApi.messages().send(mandrillMessage, true);
            logger.info(String.format("mail sent to %s", rawMailMessage.recipients.toString()));
            return appendMailStatusus(mailStatus, mandrillMessageStatuses);
        } catch (MandrillApiError mandrillApiError) {
            logger.error("MandrilMailservice API error {}", mandrillApiError.getMandrillErrorMessage());
            throw new IllegalStateException(mandrillApiError);
        } catch (IOException e) {
            logger.error("MandrilMailservice IO exception {}", e.getMessage());
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Optional<List<MailMessageStatus>> send(TemplatedMailMessage templatedMailMessage) {
        Optional<TemplatedMailMessage> optionalMailMessage = applyFilters(templatedMailMessage);
        if (optionalMailMessage.isEmpty()) {
            logger.info("Mail message could not be sent; one or more filters caused the templatedMailMessage to be invalid");
            return Optional.empty();
        }
        var mandrillMessage = mailMessageConverter.convert(templatedMailMessage);
        var mandrillApi = getMandrillApi(templatedMailMessage.isTest);
        var mergeVars = getMergeVars(templatedMailMessage);
        var mergeVarsPerRecipient = getMergeVarBuckets(templatedMailMessage, mergeVars);
        mandrillMessage.setMerge(true);
        mandrillMessage.setMergeVars(mergeVarsPerRecipient);

        Optional<List<MailMessageStatus>> mailStatus = Optional.empty();

        try {
            MandrillMessageStatus[] mandrillMessageStatuses = mandrillApi.messages()
                .sendTemplate(templatedMailMessage.templateIdentifier.get(), null, mandrillMessage, true);
            logger.info(String.format("mail sent to %s", templatedMailMessage.recipients.toString()));
            return appendMailStatusus(mailStatus, mandrillMessageStatuses);
        } catch (MandrillApiError mandrillApiError) {
            logger.error("MandrillMailService API error {}", mandrillApiError.getMandrillErrorMessage());
            throw new IllegalStateException(mandrillApiError);
        } catch (IOException e) {
            logger.error("MandrillMailService IO exception {}", e.getMessage());
            throw new IllegalStateException(e);
        }

    }

    @Override
    public int getMaximumSizeAttachments() {
        return MAX_SIZE_ATTACHMENTS;
    }

    /**
     * A Message (raw or templated) has a property `isTest`.
     * Depending on this it will use the test api key or
     * the regular api key.
     *
     * <p>The test api key will not send out emails.
     *
     * @param isTest whether or not a message is a test
     * @return the MandrillApi object instantiated with the correct api key
     */
    private MandrillApi getMandrillApi(boolean isTest) {
        String mandrillApiKey = isTest ? mandrillProperties.getApiTestKey() : mandrillProperties.getApiKey();
        return new MandrillApi(mandrillApiKey);
    }

    private Optional<RawMailMessage> applyFilters(RawMailMessage rawMailMessage) {
        RawMailMessage filteredRawMailMessage = rawMailMessage;

        Collection<MailFilter> enabledMailFiltersSortedByPriority = getEnabledMailFiltersSortedByPriority();

        for (MailFilter mailFilter : enabledMailFiltersSortedByPriority) {
            Optional<RawMailMessage> optionalFilteredRawMailMessage = mailFilter.doFilter(filteredRawMailMessage);

            if (optionalFilteredRawMailMessage.isPresent()) {
                filteredRawMailMessage = optionalFilteredRawMailMessage.get();
            } else {
                return Optional.empty();
            }
        }

        return Optional.of(filteredRawMailMessage);
    }

    private Optional<TemplatedMailMessage> applyFilters(TemplatedMailMessage templatedMailMessage) {
        TemplatedMailMessage filteredTemplatedMailMessage = templatedMailMessage;

        Collection<MailFilter> enabledMailFiltersSortedByPriority = getEnabledMailFiltersSortedByPriority();

        for (MailFilter mailFilter : enabledMailFiltersSortedByPriority) {
            Optional<TemplatedMailMessage> optionalFilteredTemplatedMailMessage = mailFilter.doFilter(filteredTemplatedMailMessage);

            if (optionalFilteredTemplatedMailMessage.isPresent()) {
                filteredTemplatedMailMessage = optionalFilteredTemplatedMailMessage.get();
            } else {
                return Optional.empty();
            }
        }

        return Optional.of(filteredTemplatedMailMessage);
    }

    private Collection<MailFilter> getEnabledMailFiltersSortedByPriority() {
        Collection<MailFilter> enabledMailFiltersSortedByPriority = mailFilters.stream()
            .filter(MailFilter::isEnabled)
            .sorted(comparing(MailFilter::getPriority))
            .collect(toList());

        return enabledMailFiltersSortedByPriority;
    }

    private List<MandrillMessage.MergeVar> getMergeVars(TemplatedMailMessage templatedMailMessage) {
        formatDateVariables(templatedMailMessage.placeholders);
        return templatedMailMessage.placeholders.entrySet()
            .stream()
            .map(entry -> new MandrillMessage.MergeVar(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());
    }

    private List<MandrillMessage.MergeVarBucket> getMergeVarBuckets(TemplatedMailMessage templatedMailMessage, List<MandrillMessage.MergeVar> mergeVars) {
        List<MandrillMessage.MergeVarBucket> mergeVarsPerRecipient = new ArrayList<>(templatedMailMessage.recipients.get().size());
        for (Recipient recipient : templatedMailMessage.recipients.get()) {
            MandrillMessage.MergeVarBucket mergeVarBucket = new MandrillMessage.MergeVarBucket();
            mergeVarBucket.setRcpt(recipient.email.get());
            mergeVarBucket.setVars(mergeVars.toArray(new MandrillMessage.MergeVar[0]));
            mergeVarsPerRecipient.add(mergeVarBucket);
        }
        return mergeVarsPerRecipient;
    }

    private Optional<List<MailMessageStatus>> appendMailStatusus(
        Optional<List<MailMessageStatus>> mailStatus,
        MandrillMessageStatus[] mandrillMessageStatuses
    ) {
        List<MailMessageStatus> mailMessageStatusList = new ArrayList<>();
        for (MandrillMessageStatus mandrillMessageStatus : mandrillMessageStatuses) {
            MailMessageStatus.Builder builder = MailMessageStatus.with(
                EmailAddress.from(mandrillMessageStatus.getEmail()), mandrillMessageStatus.getStatus(), mandrillMessageStatus.getId()
            );
            if (mandrillMessageStatus.getRejectReason() != null && !mandrillMessageStatus.getRejectReason().isEmpty()) {
                builder.rejectReason(mandrillMessageStatus.getRejectReason());
            }
            mailMessageStatusList.add(builder.build());
        }
        if (!mailMessageStatusList.isEmpty()) {
            mailStatus = Optional.of(mailMessageStatusList);
        }
        return mailStatus;
    }

    private void formatDateVariables(Map<String, Object> variables) {
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            if (entry.getValue() instanceof Date) {
                entry.setValue(dateFormat.format((Date) entry.getValue()));
            } else if (entry.getValue() instanceof Map) {
                formatDateVariables((Map<String, Object>) entry.getValue());
            }
        }
    }

}