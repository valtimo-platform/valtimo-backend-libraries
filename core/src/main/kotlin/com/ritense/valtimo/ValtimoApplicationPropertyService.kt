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

package com.ritense.valtimo

import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.config.ValtimoProperties
import com.ritense.valtimo.domain.ValtimoApplicationProperty
import com.ritense.valtimo.repository.ValtimoApplicationPropertyRepository
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrNull

@Service
@SkipComponentScan
class ValtimoApplicationPropertyService(
    val repository: ValtimoApplicationPropertyRepository,
    val valtimoProperties: ValtimoProperties
) {
    @PostConstruct
    fun databaseValidation() {
        val identifierField = repository.findById("identifierField").getOrNull()
        if (identifierField == null) {
            repository.save(
                ValtimoApplicationProperty(
                    "identifierField",
                    valtimoProperties.oauth.identifierField.toString()
                )
            )
        } else {
            require(identifierField.propertyValue == valtimoProperties.oauth.identifierField.toString())
        }
    }
}