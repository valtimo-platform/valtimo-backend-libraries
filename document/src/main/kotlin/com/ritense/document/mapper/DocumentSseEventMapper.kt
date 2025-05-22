/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

package com.ritense.document.mapper

import com.ritense.document.domain.event.CaseAssignedEvent
import com.ritense.document.domain.event.CaseCreatedEvent
import com.ritense.document.domain.event.CaseUnassignedEvent
import com.ritense.document.event.DocumentAssigned
import com.ritense.document.event.DocumentCreated
import com.ritense.document.event.DocumentUnassigned
import com.ritense.inbox.ValtimoEvent
import com.ritense.valtimo.web.sse.domain.SseEventMapper
import com.ritense.valtimo.web.sse.event.BaseSseEvent

class DocumentSseEventMapper : SseEventMapper {

    override fun map(event: ValtimoEvent): BaseSseEvent? {
        return when (event.type) {
            DocumentCreated.TYPE -> CaseCreatedEvent()
            DocumentAssigned.TYPE -> CaseAssignedEvent()
            DocumentUnassigned.TYPE -> CaseUnassignedEvent()
            else -> null
        }
    }
}