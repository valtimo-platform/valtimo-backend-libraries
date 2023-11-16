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

package com.ritense.idempotency.domain

import java.time.LocalDateTime
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import javax.persistence.UniqueConstraint

@Entity
@Table(name = "idempotent_message", uniqueConstraints = [UniqueConstraint(columnNames = ["consumer", "message_id"])])
class IdempotentMessage(

    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)", updatable = false)
    val id: UUID,

    @Column(name = "consumer", columnDefinition = "VARCHAR(150)", updatable = false)
    val consumer: String,

    @Column(name = "message_id", columnDefinition = "VARCHAR(150)", updatable = false)
    val messageId: String,

    @Column(name = "processed_on", columnDefinition = "DATETIME", updatable = false)
    val processedOn: LocalDateTime
)