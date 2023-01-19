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

package com.ritense.zakenapi.domain

import com.fasterxml.jackson.annotation.JsonProperty
import com.ritense.valtimo.contract.repository.UriAttributeConverter
import com.ritense.valtimo.contract.validation.Validatable
import org.springframework.data.domain.Persistable
import java.net.URI
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.EmbeddedId
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "zaak_instance_link")
data class ZaakInstanceLink(

    @EmbeddedId
    val zaakInstanceLinkId: ZaakInstanceLinkId,

    @Convert(converter = UriAttributeConverter::class)
    @Column(name = "zaak_instance_url", columnDefinition = "VARCHAR(512)", nullable = false)
    val zaakInstanceUrl: URI,

    @Column(name = "zaak_instance_id", nullable = false)
    val zaakInstanceId: UUID,


    @Column(name = "document_id", nullable = false)
    val documentId: UUID,

    @Convert(converter = UriAttributeConverter::class)
    @Column(name = "zaak_type_url", columnDefinition = "VARCHAR(512)", nullable = false)
    val zaakTypeUrl: URI
) : Persistable<ZaakInstanceLinkId>, Validatable {

    init {
        validate()
    }

    @JsonProperty("id")
    override fun getId(): ZaakInstanceLinkId {
        return zaakInstanceLinkId
    }

    override fun isNew(): Boolean {
        return zaakInstanceLinkId.isNew
    }
}
