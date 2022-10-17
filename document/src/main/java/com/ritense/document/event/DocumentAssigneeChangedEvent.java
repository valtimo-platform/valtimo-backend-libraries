/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

package com.ritense.document.event;

import com.ritense.valtimo.contract.audit.AuditEvent;
import com.ritense.valtimo.contract.audit.AuditMetaData;
import java.time.LocalDateTime;
import java.util.UUID;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotNull;

public class DocumentAssigneeChangedEvent extends AuditMetaData
    implements AuditEvent {

    private String assigneeName;

    public DocumentAssigneeChangedEvent(UUID id,
                                        String origin,
                                        LocalDateTime occuredOn,
                                        String user,
                                        String assigneeName) {
        super(id, origin, occuredOn, user);
        assertArgumentNotNull(assigneeName, "assignee name is required");
    }

    public String getAssigneeName() {
        return assigneeName;
    }

    public void setAssigneeName(String assigneeName) {
        this.assigneeName = assigneeName;
    }
}
