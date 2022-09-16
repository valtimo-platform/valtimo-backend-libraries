/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

package com.ritense.openzaak.plugin

import com.ritense.catalogiapi.service.ZaaktypeUrlProvider
import com.ritense.openzaak.service.ZaakTypeLinkService
import com.ritense.openzaak.service.impl.ZaakInstanceLinkService
import com.ritense.zakenapi.ZaakUrlProvider
import java.net.URI
import java.util.UUID

class OpenZaakUrlProvider(
    val zaakInstanceLinkService: ZaakInstanceLinkService,
    val zaakTypeLinkService: ZaakTypeLinkService
): ZaakUrlProvider, ZaaktypeUrlProvider {
    override fun getZaak(documentId: UUID): String {
        return zaakInstanceLinkService.getByDocumentId(documentId).zaakInstanceUrl.toString()
    }

    override fun getZaaktypeUrl(documentDefinitionName: String): URI {
        val zaakTypeLink = zaakTypeLinkService.get(documentDefinitionName)
        requireNotNull(zaakTypeLink) {
            "No zaak type was found for document definition with name $documentDefinitionName"
        }
        return zaakTypeLink.zaakTypeUrl
    }
}