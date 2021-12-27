/*
 *
 *  * Copyright 2015-2021 Ritense BV, the Netherlands.
 *  *
 *  * Licensed under EUPL, Version 1.2 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" basis,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.ritense.openzaak.domain.mapping.impl

import com.fasterxml.jackson.annotation.JsonValue
import com.ritense.valtimo.contract.domain.AbstractId
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Embeddable
import org.hibernate.annotations.Type

@Embeddable
class ZaakInstanceLinkId (

    @Column(name = "zaak_instance_link_id")
    @JsonValue
    val id: UUID

) : AbstractId<ZaakInstanceLinkId>() {

    companion object {

        fun existingId(id: UUID): ZaakInstanceLinkId {
            return ZaakInstanceLinkId(id)
        }

        fun newId(id: UUID): ZaakInstanceLinkId {
            return ZaakInstanceLinkId(id).newIdentity()
        }

    }
}