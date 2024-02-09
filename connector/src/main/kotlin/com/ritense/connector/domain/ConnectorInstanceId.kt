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

package com.ritense.connector.domain

import com.fasterxml.jackson.annotation.JsonValue
import com.ritense.valtimo.contract.domain.AbstractId
import java.util.UUID
import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Deprecated("Since 12.0.0")
@Embeddable
data class ConnectorInstanceId(

    @Column(name = "connector_instance_id", updatable = false)
    @JsonValue
    val id: UUID

) : AbstractId<ConnectorInstanceId>() {

    companion object {

        fun existingId(id: UUID): ConnectorInstanceId {
            return ConnectorInstanceId(id)
        }

        fun newId(id: UUID): ConnectorInstanceId {
            return ConnectorInstanceId(id).newIdentity()
        }
    }
}