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

package com.ritense.audit.domain.listener.impl;

import com.ritense.audit.domain.listener.AuditEventListener;
import com.ritense.audit.exception.AuditRecordAlreadyProcessedException;
import com.ritense.audit.service.AuditEventProcessor;
import com.ritense.valtimo.contract.audit.AuditEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuditEventListenerImpl implements AuditEventListener {

    private static final Logger logger = LoggerFactory.getLogger(AuditEventListenerImpl.class);
    private final AuditEventProcessor auditEventProcessor;

    public AuditEventListenerImpl(AuditEventProcessor auditEventProcessor) {
        this.auditEventProcessor = auditEventProcessor;
    }

    public void handle(AuditEvent event) {
        logger.debug("Enter: {}.{} with argument[s] = {}",
            AuditEventListenerImpl.class,
            "handle(AuditEvent event)",
            event
        );
        try {
            auditEventProcessor.process(event);
        } catch (AuditRecordAlreadyProcessedException e) {
            logger.debug("Notice: handling AuditEvent - {} - {}", event, e.getMessage());
        } catch (Exception e) {
            logger.error("Error handling AuditEvent ", e);
        }
    }

}