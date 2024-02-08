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

package com.ritense.openzaak.domain.mapping.impl

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.ritense.openzaak.repository.converter.UriAttributeConverter
import com.ritense.valtimo.contract.validation.Validatable
import org.hibernate.validator.constraints.Length
import org.springframework.data.domain.Persistable
import java.net.URI
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.validation.constraints.NotBlank

@Entity
@Table(name = "informatie_object_type_link")
@Deprecated("Since 12.0.0. Use the DocumentenApiPlugin instead")
data class InformatieObjectTypeLink(

    @EmbeddedId
    @JsonProperty("id")
    val informatieObjectTypeLinkId: InformatieObjectTypeLinkId,

    @Column(name = "document_definition_name", columnDefinition = "VARCHAR(50)", nullable = false)
    @field:Length(max = 50)
    @field:NotBlank
    val documentDefinitionName: String,

    @Convert(converter = UriAttributeConverter::class)
    @Column(name = "zaak_type_url", columnDefinition = "VARCHAR(512)", nullable = false)
    var zaakType: URI,

    @Convert(converter = UriAttributeConverter::class)
    @Column(name = "informatie_object_type_url", columnDefinition = "VARCHAR(512)", nullable = false)
    var informatieObjectType: URI

) : Persistable<InformatieObjectTypeLinkId>, Validatable {

    init {
        validate()
    }

    @JsonIgnore
    override fun getId(): InformatieObjectTypeLinkId {
        return informatieObjectTypeLinkId
    }

    @JsonIgnore
    override fun isNew(): Boolean {
        return informatieObjectTypeLinkId.isNew
    }

    fun change(informatieObjectType: URI) {
        this.informatieObjectType = informatieObjectType
    }
}