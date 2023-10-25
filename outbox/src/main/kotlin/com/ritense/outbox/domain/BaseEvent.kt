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

package com.ritense.outbox.domain
import com.fasterxml.jackson.databind.node.ObjectNode
import java.time.LocalDateTime
import java.util.*

abstract class BaseEvent (
    open val id: UUID  = UUID.randomUUID(),
    open val specversion: String,
    open val type: String,
    open val date: LocalDateTime = LocalDateTime.now(),
    open val userId: String? = null,
    open val roles: String? = null,
    open val resultType: String?,
    open val resultId: String?,
    open val result: ObjectNode,
)