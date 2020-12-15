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

package com.ritense.valtimo.contract.audit;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonView;
import com.ritense.valtimo.contract.audit.view.AuditView;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Definition of AuditEvent.
 * This can be use anywhere in a Valtimo impl when publishing events via the {@link org.springframework.context.ApplicationEventPublisher}.
 * Once published the Audit module (if enabled) will handle the event.
 *
 * @author Tom Bokma
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "className")
public interface AuditEvent {
    /**
     * The unique id. Used as primary key in storage.
     *
     * @return UUID
     */
    @JsonView(AuditView.Internal.class)
    UUID getId();

    /**
     * IP preferable, otherwise "unidentified"  .
     * See also com.ritense.common.utils.IpUtils note: this extracts also X_FORWARDED_FOR requests.
     *
     * @return String
     */
    @JsonView(AuditView.Internal.class)
    String getOrigin();

    /**
     * Date of occurrence.
     *
     * @return LocalDateTime
     */
    @JsonView(AuditView.Public.class)
    LocalDateTime getOccurredOn();

    /**
     * Username preferable, otherwise anonymous. Tip use the securityUtils to determine the user.
     * SecurityUtils.getCurrentUserLogin();
     *
     * @return String
     */
    @JsonView(AuditView.Public.class)
    String getUser();

}