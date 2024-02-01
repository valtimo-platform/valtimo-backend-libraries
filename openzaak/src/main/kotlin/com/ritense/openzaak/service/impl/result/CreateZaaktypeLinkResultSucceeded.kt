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

import com.ritense.openzaak.domain.mapping.impl.ZaakTypeLink
import com.ritense.openzaak.service.result.CreateZaakTypeLinkResult
import com.ritense.openzaak.service.result.error.ZaakTypeLinkOperationError

@Deprecated("Will not be replaced. See zaken-api module for ZaakTypeLink functionality")
data class CreateZaaktypeLinkResultSucceeded(
    private val zaakTypeLink: ZaakTypeLink
) : CreateZaakTypeLinkResult {

    override fun zaakTypeLink(): ZaakTypeLink? {
        return zaakTypeLink
    }

    override fun errors(): List<ZaakTypeLinkOperationError> {
        return emptyList()
    }

}