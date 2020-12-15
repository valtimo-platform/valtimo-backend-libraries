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

package com.ritense.audit.service.impl;

import com.ritense.valtimo.contract.audit.AuditEvent;

import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotEmpty;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotNull;

public class SearchCriteria {

    private final String path;
    private final Class<? extends AuditEvent> auditEvent;
    private final String value;

    public SearchCriteria(
        final String path,
        final Class<? extends AuditEvent> auditEvent,
        final String value
    ) {
        assertArgumentNotEmpty(path, "path is required");
        assertArgumentNotNull(auditEvent, "auditEvent is required");
        assertArgumentNotEmpty(value, "value is required");
        this.path = path;
        this.auditEvent = auditEvent;
        this.value = value;
    }

    public String getPath() {
        return path;
    }

    public Class<? extends AuditEvent> getAuditEvent() {
        return auditEvent;
    }

    public String getValue() {
        return value;
    }
}
