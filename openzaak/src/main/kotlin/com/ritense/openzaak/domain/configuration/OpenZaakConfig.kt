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

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.ritense.openzaak.domain.request.ModifyOpenZaakConfigRequest
import com.ritense.valtimo.contract.validation.Validatable
import org.hibernate.validator.constraints.Length
import org.springframework.data.domain.Persistable
import javax.persistence.Column
import javax.persistence.Embedded
import javax.persistence.EmbeddedId
import javax.persistence.Entity
import javax.persistence.Table
import javax.validation.constraints.NotBlank

@Entity
@Table(name = "open_zaak_config")
data class OpenZaakConfig(

    @EmbeddedId
    @JsonProperty("id")
    val openZaakConfigId: OpenZaakConfigId,

    @Column(name = "url", columnDefinition = "VARCHAR(1024)", nullable = false)
    @field:Length(max = 1024)
    @field:NotBlank
    var url: String,

    @Column(name = "client_id", columnDefinition = "VARCHAR(255)", nullable = false)
    @field:Length(max = 255)
    @field:NotBlank
    var clientId: String,

    @Embedded
    var secret: Secret,

    @Embedded
    var rsin: Rsin

) : Persistable<OpenZaakConfigId>, Validatable {

    init {
        validate()
    }

    fun changeConfig(request: ModifyOpenZaakConfigRequest) {
        this.url = request.url
        this.clientId = request.clientId
        this.secret = Secret(request.secret)
        this.rsin = Rsin(request.rsin)
        validate()
    }

    /*Persistable related*/
    @JsonIgnore
    override fun getId(): OpenZaakConfigId {
        return openZaakConfigId
    }

    @JsonIgnore
    override fun isNew(): Boolean {
        return openZaakConfigId.isNew
    }

}