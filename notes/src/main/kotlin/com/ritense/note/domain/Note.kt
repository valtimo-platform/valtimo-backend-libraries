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

package com.ritense.note.domain

import com.ritense.document.domain.impl.JsonSchemaDocumentId
import com.ritense.valtimo.contract.authentication.ManageableUser
import java.time.LocalDateTime
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "note")
data class Note(

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    val id: UUID,

    @Column(name = "created_by_user_id", nullable = false, length = 255, updatable = false)
    val createdByUserId: String,

    @Column(name = "created_by_user_full_name", nullable = false, length = 255, updatable = false)
    val createdByUserFullName: String,

    @Column(name = "created_date", columnDefinition = "DATETIME", nullable = false)
    val createdDate: LocalDateTime,

    @Column(name = "content", columnDefinition = "CLOB", nullable = false)
    val content: String,

    @Column(name = "document_id", nullable = false)
    val documentId: UUID,
) {
    constructor(documentId: JsonSchemaDocumentId, user: ManageableUser, content: String) : this(
        UUID.randomUUID(),
        user.id,
        user.fullName,
        LocalDateTime.now(),
        content,
        documentId.id,
    )
}
