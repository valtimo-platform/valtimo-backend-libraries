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

package com.ritense.zakenapi.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.valtimo.contract.domain.AggregateRoot
import com.ritense.valtimo.contract.domain.DomainEvent
import com.ritense.valtimo.contract.repository.UriAttributeConverter
import com.ritense.valtimo.contract.validation.Validatable
import com.ritense.zakenapi.web.rest.request.CreateZaakTypeLinkRequest
import com.ritense.zgw.Rsin
import com.ritense.zgw.converter.RsinAttributeConverter
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.Length
import org.springframework.data.domain.Persistable
import java.net.URI
import java.util.UUID

@Entity
@Table(name = "zaak_type_link")
data class ZaakTypeLink(

    @EmbeddedId
    @JsonProperty("id")
    val zaakTypeLinkId: ZaakTypeLinkId,

    @Column(name = "document_definition_name", columnDefinition = "VARCHAR(50)", nullable = false)
    @field:Length(max = 50)
    @field:NotBlank
    val documentDefinitionName: String,

    @Convert(converter = UriAttributeConverter::class)
    @Column(name = "zaak_type_url", columnDefinition = "VARCHAR(512)", nullable = false)
    var zaakTypeUrl: URI,

    @Column(name = "create_with_dossier", columnDefinition = "BOOLEAN", nullable = false)
    var createWithDossier: Boolean = false,

    @Column(name = "zaken_api_plugin_configuration_id", nullable = true)
    var zakenApiPluginConfigurationId: UUID? = null,

    @Convert(converter = RsinAttributeConverter::class)
    @Column(name = "rsin", nullable = true)
    var rsin: Rsin? = null,
) : Persistable<ZaakTypeLinkId>, Validatable, AggregateRoot<DomainEvent>() {

    init {
        require(zakenApiPluginConfigurationId == null || rsin != null) { "RSIN is required" }
        validate()
    }

    fun processUpdateRequest(request: CreateZaakTypeLinkRequest) {
        require(request.zakenApiPluginConfigurationId == null || request.rsin != null) { "RSIN is required" }
        this.zaakTypeUrl = request.zaakTypeUrl
        this.zakenApiPluginConfigurationId = request.zakenApiPluginConfigurationId
        this.rsin = request.rsin?.let { Rsin(it) }
        request.createWithDossier?.let { this.createWithDossier = it }
    }

    @JsonIgnore
    override fun getId(): ZaakTypeLinkId {
        return zaakTypeLinkId
    }

    @JsonIgnore
    override fun isNew(): Boolean {
        return zaakTypeLinkId.isNew
    }
}
