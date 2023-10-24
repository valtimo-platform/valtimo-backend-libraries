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
import java.util.UUID
import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.valtimo.contract.utils.SecurityUtils
import java.time.LocalDateTime

abstract class BaseEvent (
    open val id: UUID  = UUID.randomUUID(),
    open var source: String?,
    open val specversion: String,
    open val type: String,
    open  val data: LocalDateTime = LocalDateTime.now(),
    open val userId: String = SecurityUtils.getCurrentUserLogin() ?: "System",
    open val roles: String = SecurityUtils.getCurrentUserRoles().joinToString(),
    open val resultType: String?,
    open val resultId: String?,
    open val result: ObjectNode,
)