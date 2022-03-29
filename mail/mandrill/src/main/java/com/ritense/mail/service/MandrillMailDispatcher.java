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
import com.ritense.mail.MailDispatcher;
import com.ritense.mail.config.MandrillProperties;
import com.ritense.valtimo.contract.basictype.EmailAddress;
import com.ritense.valtimo.contract.mail.model.MailMessageStatus;
import com.ritense.valtimo.contract.mail.model.RawMailMessage;
import com.ritense.valtimo.contract.mail.model.TemplatedMailMessage;
import com.ritense.valtimo.contract.mail.model.value.Recipient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MandrillMailDispatcher implements MailDispatcher {

    //see: https://mandrill.zendesk.com/hc/en-us/articles/205582407-Does-Mandrill-Support-Attachments-
    private static final int MAX_SIZE_ATTACHMENTS = 16250000;
    private static final Logger logger = LoggerFactory.getLogger(MandrillMailDispatcher.class);

    private final DateFormat dateFormat;
    private final MandrillProperties mandrillProperties;
    private final MailMessageConverter mailMessageConverter;

    public MandrillMailDispatcher(
        final MandrillProperties mandrillProperties,
        final MailMessageConverter mailMessageConverter
    ) {
        this.mandrillProperties = mandrillProperties;
        this.mailMessageConverter = mailMessageConverter;
        this.dateFormat = new SimpleDateFormat(mandrillProperties.getDateFormat());
    }

    @Override
    public List<MailMessageStatus> send(RawMailMessage rawMailMessage) {
        var mandrillMessage = mailMessageConverter.convert(rawMailMessage);
        var mandrillApi = getMandrillApi(rawMailMessage.isTest);
        List<MailMessageStatus> mailStatus = new ArrayList();

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
    public List<MailMessageStatus> send(TemplatedMailMessage templatedMailMessage) {
        var mandrillMessage = mailMessageConverter.convert(templatedMailMessage);
        var mandrillApi = getMandrillApi(templatedMailMessage.isTest);
        var mergeVars = getMergeVars(templatedMailMessage);
        var mergeVarsPerRecipient = getMergeVarBuckets(templatedMailMessage, mergeVars);
        mandrillMessage.setMerge(true);
        mandrillMessage.setMergeVars(mergeVarsPerRecipient);

        List<MailMessageStatus> mailStatus = new ArrayList();

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

    private List<MailMessageStatus> appendMailStatusus(
        List<MailMessageStatus> mailStatus,
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
            mailStatus = mailMessageStatusList;
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