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

package com.ritense.valtimo.accessandentitlement.domain.listener;

import com.ritense.valtimo.accessandentitlement.domain.Authority;
import com.ritense.valtimo.accessandentitlement.domain.event.AuthorityDeletedEvent;
import com.ritense.valtimo.contract.audit.utils.AuditHelper;
import com.ritense.valtimo.contract.utils.RequestHelper;
import org.springframework.context.ApplicationEventPublisher;
import javax.persistence.PostRemove;
import java.time.LocalDateTime;
import java.util.UUID;

public class AuthorityDeletedEventListener {

    private final ApplicationEventPublisher applicationEventPublisher;

    public AuthorityDeletedEventListener(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @PostRemove
    public void handle(Authority authority) {
        applicationEventPublisher.publishEvent(
            new AuthorityDeletedEvent(
                UUID.randomUUID(),
                RequestHelper.getOrigin(),
                LocalDateTime.now(),
                AuditHelper.getActor(),
                authority.getName(),
                authority.getSystemAuthority()
            )
        );
    }

}