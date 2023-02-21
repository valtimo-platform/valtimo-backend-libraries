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

package com.ritense.valtimo.contract.mail.model;

import com.ritense.valtimo.contract.mail.model.value.Attachment;
import com.ritense.valtimo.contract.mail.model.value.AttachmentCollection;
import com.ritense.valtimo.contract.mail.model.value.MailBody;
import com.ritense.valtimo.contract.mail.model.value.Recipient;
import com.ritense.valtimo.contract.mail.model.value.RecipientCollection;
import com.ritense.valtimo.contract.mail.model.value.Sender;
import com.ritense.valtimo.contract.mail.model.value.Subject;
import java.util.Objects;

/**
 * Inspired by MandrillMessage.
 */
public class RawMailMessage {
    public final Subject subject;
    public final MailBody mailBody;
    public final Sender sender;
    public final RecipientCollection recipients;
    public final AttachmentCollection attachments;
    public final boolean isTest;

    private RawMailMessage(Builder builder) {
        this.subject = builder.subject;
        this.sender = builder.sender;
        this.recipients = builder.recipients;
        this.mailBody = builder.mailBody;
        this.attachments = builder.attachments;
        this.isTest = builder.isTest;
    }

    public static Builder with(RecipientCollection recipients, MailBody mailBody) {
        return new Builder(recipients, mailBody);
    }

    public static Builder with(Recipient recipient, MailBody mailBody) {
        return new Builder(RecipientCollection.fromSingle(recipient), mailBody);
    }

    public static class Builder {
        private final MailBody mailBody;
        private final RecipientCollection recipients;

        private Subject subject = Subject.none();
        private Sender sender = Sender.empty();
        private AttachmentCollection attachments = AttachmentCollection.empty();
        private boolean isTest = false;

        private Builder(RecipientCollection recipients, MailBody mailBody) {
            Objects.requireNonNull(recipients, "Recipient Collection argument cannot be null");
            Objects.requireNonNull(mailBody, "MailBody argument cannot be null");

            this.recipients = recipients;
            this.mailBody = mailBody;
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

        public RawMailMessage build() {
            return new RawMailMessage(this);
        }
    }
}
