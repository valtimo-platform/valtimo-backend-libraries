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

package com.ritense.zakenapi.web.rest

import com.ritense.logging.LoggableResource
import com.ritense.logging.withLoggingContext
import com.ritense.zakenapi.domain.ZaakTypeLink
import com.ritense.zakenapi.service.ZaakTypeLinkService
import com.ritense.zakenapi.web.rest.request.CreateZaakTypeLinkRequest
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.noContent
import org.springframework.http.ResponseEntity.ok

class DefaultZaakTypeLinkResource(
    private val zaakTypeLinkService: ZaakTypeLinkService
) : ZaakTypeLinkResource {

    override fun get(
        @LoggableResource("documentDefinitionName") documentDefinitionName: String
    ): ResponseEntity<ZaakTypeLink?> {
        return when (val zaakTypeLink = zaakTypeLinkService.get(documentDefinitionName)) {
            null -> noContent().build()
            else -> ok(zaakTypeLink)
        }
    }

    override fun getByProcess(
        @LoggableResource("processDefinitionKey") processDefinitionKey: String
    ): ResponseEntity<List<ZaakTypeLink>> {
        return ok(zaakTypeLinkService.getByProcess(processDefinitionKey))
    }

    override fun create(request: CreateZaakTypeLinkRequest): ResponseEntity<ZaakTypeLink> {
        return withLoggingContext("documentDefinitionName", request.documentDefinitionName) {
            val result = zaakTypeLinkService.createZaakTypeLink(request)
            ok(result)
        }
    }

    override fun remove(
        @LoggableResource("documentDefinitionName") documentDefinitionName: String
    ): ResponseEntity<Void> {
        zaakTypeLinkService.deleteZaakTypeLinkBy(documentDefinitionName)
        return noContent().build()
    }
}