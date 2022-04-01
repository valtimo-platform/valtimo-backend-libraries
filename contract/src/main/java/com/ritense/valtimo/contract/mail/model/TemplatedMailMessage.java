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

package com.ritense.valtimo.contract.mail.model;

import com.ritense.valtimo.contract.mail.model.value.Attachment;
import com.ritense.valtimo.contract.mail.model.value.AttachmentCollection;
import com.ritense.valtimo.contract.mail.model.value.MailTemplateIdentifier;
import com.ritense.valtimo.contract.mail.model.value.Recipient;
import com.ritense.valtimo.contract.mail.model.value.RecipientCollection;
import com.ritense.valtimo.contract.mail.model.value.Sender;
import com.ritense.valtimo.contract.mail.model.value.Subject;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public class TemplatedMailMessage {
    public final Subject subject;
    public final Sender sender;
    public final RecipientCollection recipients;
    public final MailTemplateIdentifier templateIdentifier;
    public final Map<String, Object> placeholders;
    public final AttachmentCollection attachments;
    public final boolean isTest;

    // Only used by Jackson
    private TemplatedMailMessage() {
        subject = null;
        sender = null;
        recipients = null;
        templateIdentifier = null;
        placeholders = null;
        attachments = null;
        isTest = false;
    }

    private TemplatedMailMessage(Builder builder) {
        this.subject = builder.subject;
        this.sender = builder.sender;
        this.recipients = builder.recipients;
        this.templateIdentifier = builder.templateIdentifier;
        this.placeholders = builder.placeholders;
        this.attachments = builder.attachments;
        this.isTest = builder.isTest;
    }

    public static Builder with(RecipientCollection recipients, MailTemplateIdentifier mailTemplateIdentifier) {
        return new Builder(recipients, mailTemplateIdentifier);
    }

    public static Builder with(Recipient recipient, MailTemplateIdentifier mailTemplateIdentifier) {
        return new Builder(RecipientCollection.fromSingle(recipient), mailTemplateIdentifier);
    }

    public static class Builder {
        // required
        private final RecipientCollection recipients;
        private final MailTemplateIdentifier templateIdentifier;

        // optional
        private Subject subject = Subject.none();
        private Map<String, Object> placeholders = Collections.emptyMap();
        private AttachmentCollection attachments = AttachmentCollection.empty();
        private Sender sender = Sender.empty();
        private boolean isTest = false;

        private Builder(RecipientCollection recipients, MailTemplateIdentifier mailTemplateIdentifier) {
            Objects.requireNonNull(recipients, "Recipient collection cannot be null");
            Objects.requireNonNull(mailTemplateIdentifier, "MailTemplateIdentifier argument cannot be null");

            this.recipients = recipients;
            this.templateIdentifier = mailTemplateIdentifier;
        }

        public Builder subject(Subject subject) {
            Objects.requireNonNull(subject, "Subject argument cannot be null");
            this.subject = subject;
            return this;
        }

        public Builder sender(Sender sender) {
            Objects.requireNonNull(sender, "Sender argument cannot be null");
            this.sender = sender;
            return this;
        }

        public Builder placeholders(Map<String, Object> placeholders) {
            Objects.requireNonNull(placeholders, "Placeholders argument cannot be null");
            this.placeholders = placeholders;
            return this;
        }

        public Builder attachments(AttachmentCollection attachments) {
            Objects.requireNonNull(attachments, "Attachments argument cannot be null");
            this.attachments = attachments;
            return this;
        }

        public Builder attachment(Attachment attachment) {
            Objects.requireNonNull(attachment, "Attachment argument cannot be null");
            this.attachments = AttachmentCollection.fromSingle(attachment);
            return this;
        }

        public Builder isTest(boolean isTest) {
            this.isTest = isTest;
            return this;
        }

        public TemplatedMailMessage build() {
            return new TemplatedMailMessage(this);
        }
    }
}
