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

package com.ritense.resource.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import com.ritense.openzaak.repository.converter.UriAttributeConverter
import com.ritense.valtimo.contract.resource.Resource
import com.ritense.valtimo.contract.validation.Validatable
import java.net.URI
import java.time.LocalDateTime
import java.util.UUID
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.hibernate.validator.constraints.Length
import org.springframework.data.domain.Persistable
import java.time.LocalDate

@Entity
@Table(name = "open_zaak_resource")
data class OpenZaakResource(
    @EmbeddedId
    val resourceId: ResourceId,

    @Convert(converter = UriAttributeConverter::class)
    @Column(name = "informatie_object_url", nullable = false, columnDefinition = "VARCHAR(512)")
    @field:NotNull
    val informatieObjectUrl: URI,

    @Column(name = "name", nullable = false, columnDefinition = "VARCHAR(255)")
    @field:Length(max = 255)
    @field:NotBlank
    val name: String,

    @Column(name = "extension", nullable = false, columnDefinition = "VARCHAR(255)")
    @field:Length(max = 255)
    @field:NotBlank
    val extension: String,

    @Column(name = "size_in_bytes", nullable = false, columnDefinition = "BIGINT")
    @field:NotNull
    val sizeInBytes: Long,

    @Column(name = "created_on", nullable = false, columnDefinition = "DATETIME")
    @field:NotNull
    val createdOn: LocalDateTime
) : Resource, Persistable<ResourceId>, Validatable {

    init {
        validate()
    }

    override fun id(): UUID {
        return resourceId.id
    }

    override fun name(): String {
        return name
    }

    override fun extension(): String {
        return extension
    }

    override fun sizeInBytes(): Long {
        return sizeInBytes
    }

    override fun createdOn(): LocalDateTime {
        return createdOn
    }

    override fun title(): String {
        TODO("Not yet implemented")
    }

    override fun status(): String {
        TODO("Not yet implemented")
    }

    override fun language(): String {
        TODO("Not yet implemented")
    }

    override fun identification(): String {
        TODO("Not yet implemented")
    }

    override fun description(): String {
        TODO("Not yet implemented")
    }

    override fun informatieobjecttype(): String {
        TODO("Not yet implemented")
    }

    override fun keywords(): List<String> {
        TODO("Not yet implemented")
    }

    override fun format(): String {
        TODO("Not yet implemented")
    }

    override fun sendDate(): LocalDate {
        TODO("Not yet implemented")
    }

    override fun receiptDate(): LocalDate {
        TODO("Not yet implemented")
    }

    override fun confidentialityLevel(): String {
        TODO("Not yet implemented")
    }

    @JsonIgnore
    override fun getId(): ResourceId {
        return resourceId
    }

    @JsonIgnore
    override fun isNew(): Boolean {
        return resourceId.isNew
    }
}