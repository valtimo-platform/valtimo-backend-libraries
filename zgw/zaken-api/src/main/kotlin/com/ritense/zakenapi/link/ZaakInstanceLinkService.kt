/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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

package com.ritense.zakenapi.link

import com.ritense.zakenapi.domain.ZaakInstanceLink
import com.ritense.zakenapi.domain.ZaakInstanceLinkId
import com.ritense.zakenapi.repository.ZaakInstanceLinkRepository
import java.net.URI
import java.util.UUID

class  ZaakInstanceLinkService(
    private val zaakInstanceLinkRepository: ZaakInstanceLinkRepository,
) {
    fun createZaakInstanceLink(zaakInstanceUrl: URI, zaakInstanceId: UUID, documentId: UUID, zaakTypeUrl: URI): ZaakInstanceLink {
        val zaakInstanceLink = ZaakInstanceLink(
            ZaakInstanceLinkId.newId(UUID.randomUUID()),
            zaakInstanceUrl,
            zaakInstanceId,
            documentId,
            zaakTypeUrl
        )

        return zaakInstanceLinkRepository.save(zaakInstanceLink)
    }

    fun findById(zaakInstanceLinkId: ZaakInstanceLinkId): ZaakInstanceLink {
        return when (val zaakInstanceLink = zaakInstanceLinkRepository.getById(zaakInstanceLinkId)) {
            null -> throw ZaakInstanceLinkNotFoundException("No ZaakInstanceLink found for id ${zaakInstanceLinkId.id}")
            else -> zaakInstanceLink
        }
    }

    fun getByDocumentId(documentId: UUID): ZaakInstanceLink {
        return zaakInstanceLinkRepository.findByDocumentId(documentId)
            ?: throw ZaakInstanceLinkNotFoundException("No ZaakInstanceLink found for document id ${documentId}")
    }

    fun getByZaakInstanceUrl(zaakInstanceUrl: URI): ZaakInstanceLink {
        return zaakInstanceLinkRepository.findByZaakInstanceUrl(zaakInstanceUrl)
            ?: throw ZaakInstanceLinkNotFoundException("No ZaakInstanceLink found for zaak instance url ${zaakInstanceUrl}")
    }
}
