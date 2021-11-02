/*
 * Copyright 2015-2020 Ritense BV, the Netherlands.
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

import com.fasterxml.jackson.annotation.JsonProperty
import com.ritense.valtimo.contract.validation.Validatable
import org.hibernate.annotations.Type
import org.hibernate.validator.constraints.Length
import javax.persistence.Column
import javax.persistence.EmbeddedId
import javax.persistence.Entity
import javax.persistence.Index
import javax.persistence.Table
import javax.validation.constraints.NotBlank

@Entity
@Table(name = "connector_type", indexes = [Index(name = "ct_name_i", columnList = "name")])
data class ConnectorType(
    @EmbeddedId
    val id: ConnectorTypeId,

    @Column(name = "name", columnDefinition = "VARCHAR(512)", nullable = false)
    @field:Length(max = 512)
    @field:NotBlank
    var name: String,

    @Column(name = "class_name", columnDefinition = "VARCHAR(1024)", nullable = false)
    @field:Length(max = 1024)
    @field:NotBlank
    var className: String,

    @Type(type = "com.vladmihalcea.hibernate.type.json.JsonStringType")
    @Column(name = "connector_properties", columnDefinition = "json")
    @JsonProperty("properties")
    var connectorProperties: ConnectorProperties
) : Validatable {

    init {
        validate()
    }
}