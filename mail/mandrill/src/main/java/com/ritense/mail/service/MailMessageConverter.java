/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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

import com.microtripit.mandrillapp.lutung.view.MandrillMessage;
import com.ritense.mail.config.MandrillProperties;
import com.ritense.valtimo.contract.mail.model.RawMailMessage;
import com.ritense.valtimo.contract.mail.model.TemplatedMailMessage;
import com.ritense.valtimo.contract.mail.model.value.Attachment;
import com.ritense.valtimo.contract.mail.model.value.AttachmentCollection;
import com.ritense.valtimo.contract.mail.model.value.Recipient;
import com.ritense.valtimo.contract.mail.model.value.RecipientCollection;
import com.ritense.valtimo.contract.mail.model.value.Sender;
import com.ritense.valtimo.contract.mail.model.value.Subject;
import org.apache.commons.codec.binary.Base64;

import java.util.List;
import java.util.stream.Collectors;

public class MailMessageConverter {

    private final MandrillProperties mandrillProperties;
    private static final Base64 base64 = new Base64();

    public MailMessageConverter(MandrillProperties mandrillProperties) {
        this.mandrillProperties = mandrillProperties;
    }

    public MandrillMessage convert(RawMailMessage rawMailMessage) {
        MandrillMessage mandrillMessageWithContent = convertBaseline(
            rawMailMessage.subject,
            rawMailMessage.sender,
            rawMailMessage.recipients,
            rawMailMessage.attachments
        );

        assert rawMailMessage.mailBody.textBody != null;
        if (rawMailMessage.mailBody.textBody.isPresent()) {
            mandrillMessageWithContent.setText(rawMailMessage.mailBody.textBody.get());
        }

        assert rawMailMessage.mailBody.htmlBody != null;
        if (rawMailMessage.mailBody.htmlBody.isPresent()) {
            mandrillMessageWithContent.setHtml(rawMailMessage.mailBody.htmlBody.get());
        }

        if (!rawMailMessage.mailBody.textBody.isPresent() && !rawMailMessage.mailBody.htmlBody.isPresent()) {
            throw new IllegalArgumentException("Cannot convert RawMailMessage into MandrillMessage: rawMailMessage not contain a text or html body");
        }

        return mandrillMessageWithContent;
    }

    public MandrillMessage convert(TemplatedMailMessage templatedMailMessage) {
        return convertBaseline(
            templatedMailMessage.subject,
            templatedMailMessage.sender,
            templatedMailMessage.recipients,
            templatedMailMessage.attachments
        );
    }

    private MandrillMessage.Recipient convert(Recipient recipient) {
        MandrillMessage.Recipient convertedRecipient = new MandrillMessage.Recipient();

        MandrillMessage.Recipient.Type mandrillRecipientType = convert(recipient.type);
        convertedRecipient.setType(mandrillRecipientType);

        convertedRecipient.setEmail(recipient.email.get());

        if (recipient.name.isPresent()) {
            convertedRecipient.setName(recipient.name.get());
        }

        return convertedRecipient;
    }

    private MandrillMessage.Recipient.Type convert(Recipient.Type type) {
        if (type == Recipient.Type.TO) {
            return MandrillMessage.Recipient.Type.TO;
        }

        if (type == Recipient.Type.CC) {
            return MandrillMessage.Recipient.Type.CC;
        }

        if (type == Recipient.Type.BCC) {
            return MandrillMessage.Recipient.Type.BCC;
        }
        String message = String.format("Cannot convert Recipient.Type value '%s' to MandrillMessage.Recipient.Type. No mapping exists", type);
        throw new IllegalArgumentException(message);
    }

    private MandrillMessage.MessageContent convert(Attachment attachment) {
        MandrillMessage.MessageContent messageContent = new MandrillMessage.MessageContent();

        messageContent.setName(attachment.name.get());
        messageContent.setType(attachment.type.get());
        messageContent.setContent(base64.encodeToString(attachment.content.get()));

        return messageContent;
    }

    private MandrillMessage convertBaseline(Subject subject, Sender sender, RecipientCollection recipients, AttachmentCollection attachments) {
        MandrillMessage mandrillMessage = new MandrillMessage();

        /* SUBJECT */
        if (subject.isPresent()) {
            mandrillMessage.setSubject(subject.get());
        } else {
            // Subject will be used as defined on mandrillapp.com application within your specified template
        }

        /* ATTACHMENTS */
        if (attachments.isPresent()) {
            List<MandrillMessage.MessageContent> messageContents = attachments.get().stream().map(this::convert).collect(Collectors.toList());
            mandrillMessage.setAttachments(messageContents);
        }

        /* SENDER */
        if (sender.email.isPresent()) {
            mandrillMessage.setFromEmail(sender.email.get());
        } else if (mandrillProperties.getFromEmailAddress().isPresent()) {
            mandrillMessage.setFromEmail(mandrillProperties.getFromEmailAddress().orElseThrow());
        } else {
            // From emailaddress will be used as defined on mandrillapp.com application within your specified template
        }

        if (sender.name.isPresent()) {
            mandrillMessage.setFromName(sender.name.get());
        } else if (mandrillProperties.getFromName().isPresent()) {
            mandrillMessage.setFromName(mandrillProperties.getFromName().orElseThrow());
        }

        /* RECIPIENTS */
        List<MandrillMessage.Recipient> recipientMandrillList = recipients.get().stream().map(this::convert).collect(Collectors.toList());
        mandrillMessage.setTo(recipientMandrillList);

        return mandrillMessage;
    }
}
