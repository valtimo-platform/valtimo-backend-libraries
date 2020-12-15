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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MailMessageStatusTest {
    private final String status = "some-external-status";
    private final String id = "some-external-id";
    private final String mailto = "info@ritense.com";
    private final String rejectionReason = "Some reason";
    private MailMessageStatus mailMessageStatus;

    @Test
    public void createMailMessageValidEmailAddress() {

        MailMessageStatus.Builder builder = validMailMessageStatusBuilder();
        mailMessageStatus = builder.build();

        assertEquals(id, mailMessageStatus.getId());
        assertEquals(status, mailMessageStatus.getStatus());
        assertEquals(mailto, mailMessageStatus.getEmail().get());
    }

    @Test
    public void createMailMessageWithNullValues() {
        assertThrows(NullPointerException.class, this::invalidMailMessageStatusBuilder);
    }

    @Test
    public void createMailMessageWithRejectionReason() {
        MailMessageStatus.Builder builder = validMailMessageStatusBuilder();
        mailMessageStatus = builder.rejectReason(rejectionReason).build();
        assertEquals(rejectionReason, mailMessageStatus.getRejectReason());
    }

    @Test
    public void createMailMessageWithNullRejectionReason() {
        MailMessageStatus.Builder builder = validMailMessageStatusBuilder();
        assertThrows(NullPointerException.class, () -> builder.rejectReason(null).build());
    }

    private MailMessageStatus.Builder invalidMailMessageStatusBuilder() {
        final EmailAddress email = null;
        final String status = null;
        final String id = null;
        return MailMessageStatus.with(email, status, id);
    }

    private MailMessageStatus.Builder validMailMessageStatusBuilder() {
        final EmailAddress email = emailAddress();
        return MailMessageStatus.with(email, status, id);
    }

    private EmailAddress emailAddress() {
        return EmailAddress.from(mailto);
    }

}