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

package com.ritense.openzaak.service.impl.result

import com.ritense.openzaak.domain.configuration.OpenZaakConfig
import com.ritense.openzaak.service.result.CreateOpenZaakConfigResult
import com.ritense.openzaak.service.result.error.OpenZaakConfigOperationError

data class CreateOpenZaakConfigResultSucceeded(
    private val openzaakConfig: OpenZaakConfig
) : CreateOpenZaakConfigResult {

    override fun openZaakConfig(): OpenZaakConfig? {
        return openzaakConfig
    }

    override fun errors(): List<OpenZaakConfigOperationError> {
        return emptyList()
    }

}