/*
 * Copyright 2020 Dimpact.
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

package com.ritense.openzaak.domain.configuration

import com.fasterxml.jackson.annotation.JsonValue
import com.ritense.valtimo.contract.domain.AbstractId
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
data class OpenZaakConfigId(

    @Column(name = "open_zaak_config_id")
    @JsonValue
    val id: UUID

) : AbstractId<OpenZaakConfigId>() {

    companion object {

        fun existingId(id: UUID): OpenZaakConfigId {
            return OpenZaakConfigId(id)
        }

        fun newId(id: UUID): OpenZaakConfigId {
            return OpenZaakConfigId(id).newIdentity()
        }

    }

}