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

package com.ritense.objectsapi.domain.sync

import com.ritense.valtimo.contract.validation.Validatable
import org.hibernate.validator.constraints.Length
import java.util.UUID
import javax.persistence.Column
import javax.persistence.EmbeddedId
import javax.persistence.Entity
import javax.persistence.Table
import javax.validation.constraints.NotBlank

@Entity
@Table(name = "object_sync_config")
data class ObjectSyncConfig(
    @EmbeddedId
    val id: ObjectSyncConfigId,

    @Column(name = "connector_instance_id", nullable = false)
    val connectorInstanceId: UUID,

    @Column(name = "enabled", columnDefinition = "BIT")
    val enabled: Boolean,

    @Column(name = "document_definition_name", length = 50, columnDefinition = "VARCHAR(50)", nullable = false)
    @field:Length(max = 50)
    @field:NotBlank
    val documentDefinitionName: String,

    @Column(name = "object_type_id", nullable = false)
    val objectTypeId: UUID
) : Validatable {

    init {
        validate()
    }
}
