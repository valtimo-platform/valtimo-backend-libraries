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

import com.ritense.valtimo.contract.basictype.EmailAddress;
import java.util.Objects;

public class MailMessageStatus {
    private final EmailAddress email;
    private final String status;
    private final String rejectReason;
    private final String id;

    private MailMessageStatus(Builder builder) {
        this.email = builder.email;
        this.status = builder.status;
        this.rejectReason = builder.rejectReason;
        this.id = builder.id;
    }

    public static Builder with(EmailAddress email, String status, String id) {
        return new Builder(email, status, id);
    }

    public static class Builder {
        private final EmailAddress email;
        private final String status;
        private final String id;

        private String rejectReason = null;

        private Builder(EmailAddress email, String status, String id) {
            Objects.requireNonNull(email, "Email argument cannot be null");
            Objects.requireNonNull(status, "Status argument cannot be null");
            Objects.requireNonNull(id, "Id argument cannot be null");

            this.email = email;
            this.status = status;
            this.id = id;
        }

        public Builder rejectReason(String rejectReason) {
            Objects.requireNonNull(rejectReason, "Reject reason argument cannot be null");

            this.rejectReason = rejectReason;
            return this;
        }

        public MailMessageStatus build() {
            return new MailMessageStatus(this);
        }
    }

    public EmailAddress getEmail() {
        return email;
    }

    public String getStatus() {
        return status;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public String getId() {
        return id;
    }
}