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
import com.ritense.openzaak.repository.converter.SecretAttributeConverter
import com.ritense.valtimo.contract.validation.Validatable
import org.hibernate.validator.constraints.Length
import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Embeddable
import javax.validation.constraints.NotBlank

@Embeddable
data class Secret(
    @Column(name = "secret", columnDefinition = "VARCHAR(128)", nullable = false)
    @Convert(converter = SecretAttributeConverter::class)
    @field:Length(max = 128)
    @field:NotBlank
    @JsonValue
    val value: String
) : Serializable, Validatable {

    init {
        validate()
    }

}