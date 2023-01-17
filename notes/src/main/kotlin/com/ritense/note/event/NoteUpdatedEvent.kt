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

package com.ritense.note.event

import com.ritense.valtimo.contract.audit.AuditEvent
import com.ritense.valtimo.contract.audit.utils.AuditHelper
import com.ritense.valtimo.contract.utils.RequestHelper
import java.time.LocalDateTime
import java.util.UUID

data class NoteUpdatedEvent(
    private val id: UUID,
    private val origin: String,
    private val occurredOn: LocalDateTime,
    private val user: String,
    val noteId: UUID,
) : AuditEvent {

    constructor(noteId: UUID) : this(
        UUID.randomUUID(),
        RequestHelper.getOrigin(),
        LocalDateTime.now(),
        AuditHelper.getActor(),
        noteId,
    )

    override fun getId(): UUID {
        return id
    }

    override fun getOrigin(): String {
        return origin
    }

    override fun getOccurredOn(): LocalDateTime {
        return occurredOn
    }

    override fun getUser(): String {
        return user
    }
}
