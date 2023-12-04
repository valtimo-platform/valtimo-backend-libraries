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

package com.ritense.outbox

import org.hibernate.cfg.AvailableSettings.JPA_LOCK_TIMEOUT
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.QueryHints
import java.util.UUID
import jakarta.persistence.LockModeType.PESSIMISTIC_WRITE
import jakarta.persistence.QueryHint

interface OutboxMessageRepository : JpaRepository<OutboxMessage, UUID> {

    /**
     * Use 'FOR UPDATE SKIP LOCKED' in sql query. But only if it's supported by the 'spring.jpa.database-platform'.
     */
    @QueryHints(value = [QueryHint(name = JPA_LOCK_TIMEOUT, value = SKIP_LOCKED)])
    @Lock(PESSIMISTIC_WRITE)
    fun findTopByOrderByCreatedOnAsc(): OutboxMessage?

    companion object {
        const val SKIP_LOCKED = "-2"
    }
}
