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

import com.fasterxml.jackson.databind.node.ObjectNode
import org.hibernate.annotations.Type
import java.time.LocalDateTime
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "outbox_message")
class OutboxMessage(

    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)", updatable = false)
    val id: UUID = UUID.randomUUID(),

    @Type(type = "com.vladmihalcea.hibernate.type.json.JsonType")
    @Column(name = "message", columnDefinition = "JSON", updatable = false)
    val message: ObjectNode,

    @Column(name = "event_type", columnDefinition = "VARCHAR(1024)", updatable = false)
    val eventType: String,

    @Column(name = "created_on", columnDefinition = "DATETIME", updatable = false)
    val createdOn: LocalDateTime = LocalDateTime.now()
)
