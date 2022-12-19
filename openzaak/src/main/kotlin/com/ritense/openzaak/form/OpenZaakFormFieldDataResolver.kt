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

package com.ritense.openzaak.form

import com.ritense.openzaak.exception.UnmappableOpenZaakPropertyException
import com.ritense.openzaak.exception.ZaakInstanceNotFoundException
import com.ritense.openzaak.service.impl.ZaakInstanceLinkService
import com.ritense.openzaak.service.impl.ZaakService
import com.ritense.valtimo.contract.form.DataResolvingContext
import com.ritense.valtimo.contract.form.FormFieldDataResolver

class OpenZaakFormFieldDataResolver(
    private val zaakService: ZaakService,
    private val zaakInstanceLinkService: ZaakInstanceLinkService
) : FormFieldDataResolver {

    override fun supports(externalFormFieldType: String): Boolean {
        return externalFormFieldType == "OpenZaak"
    }

    override fun get(
        dataResolvingContext: DataResolvingContext,
        vararg varNames: String
    ): Map<String, Any> {
        val result = mutableMapOf<String, String>()
        try {
            val zaakInstanceLink = zaakInstanceLinkService.getByDocumentId(dataResolvingContext.documentId)
            val eigenschappen = zaakService.getZaakEigenschappen(zaakInstanceLink.zaakInstanceId)

            if (eigenschappen.isNotEmpty()) {
                for (varName in varNames) {
                    val eigenschap = eigenschappen.find { it.naam == varName }
                    if (eigenschap != null) {
                        result[varName] = eigenschap.waarde
                    }
                }
            }
        } catch (e: ZaakInstanceNotFoundException) {
            throw UnmappableOpenZaakPropertyException("No zaak instance link available even though oz prefix is used in form")
        }
        return result
    }
}