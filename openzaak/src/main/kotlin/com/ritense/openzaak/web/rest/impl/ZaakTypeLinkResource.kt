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

package com.ritense.openzaak.web.rest.impl

import com.ritense.openzaak.domain.mapping.impl.ZaakTypeLink
import com.ritense.openzaak.domain.mapping.impl.ZaakTypeLinkId
import com.ritense.openzaak.domain.request.CreateZaakTypeLinkRequest
import com.ritense.openzaak.service.impl.ZaakTypeLinkService
import com.ritense.openzaak.service.result.CreateServiceTaskHandlerResult
import com.ritense.openzaak.service.result.CreateZaakTypeLinkResult
import com.ritense.openzaak.service.result.ModifyServiceTaskHandlerResult
import com.ritense.openzaak.service.result.RemoveServiceTaskHandlerResult
import com.ritense.openzaak.web.rest.ZaakTypeLinkResource
import com.ritense.openzaak.web.rest.request.ServiceTaskHandlerRequest
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.noContent
import org.springframework.http.ResponseEntity.ok
import java.util.UUID

class ZaakTypeLinkResource(
    private val zaakTypeLinkService: ZaakTypeLinkService
) : ZaakTypeLinkResource {

    override fun get(documentDefinitionName: String): ResponseEntity<ZaakTypeLink?> {
        return when (val zaakTypeLink = zaakTypeLinkService.get(documentDefinitionName)) {
            null -> noContent().build()
            else -> ok(zaakTypeLink)
        }
    }

    override fun getByProcess(processDefinitionKey: String): ResponseEntity<List<ZaakTypeLink?>> {
        return ok(zaakTypeLinkService.getByProcess(processDefinitionKey))
    }

    override fun create(request: CreateZaakTypeLinkRequest): ResponseEntity<CreateZaakTypeLinkResult> {
        val result = zaakTypeLinkService.createZaakTypeLink(request)
        return when (result.zaakTypeLink()) {
            null -> ResponseEntity.badRequest().body(result)
            else -> ok(result)
        }
    }

    override fun remove(documentDefinitionName: String): ResponseEntity<ZaakTypeLink?> {
        zaakTypeLinkService.deleteZaakTypeLinkBy(documentDefinitionName)
        return noContent().build()
    }

    override fun createServiceTaskHandler(
        id: UUID,
        request: ServiceTaskHandlerRequest
    ): ResponseEntity<CreateServiceTaskHandlerResult> {
        val zaakTypeLinkId = ZaakTypeLinkId.existingId(id)

        val result = zaakTypeLinkService.assignServiceTaskHandler(zaakTypeLinkId, request)
        return when (result.zaakTypeLink()) {
            null -> ResponseEntity.badRequest().body(result)
            else -> ok(result)
        }
    }

    override fun modifyServiceTaskHandler(
        id: UUID,
        request: ServiceTaskHandlerRequest
    ): ResponseEntity<ModifyServiceTaskHandlerResult> {
        val zaakTypeLinkId = ZaakTypeLinkId.existingId(id)

        val result = zaakTypeLinkService.modifyServiceTaskHandler(zaakTypeLinkId, request)
        return when (result.zaakTypeLink()) {
            null -> ResponseEntity.badRequest().body(result)
            else -> ok(result)
        }

    }

    override fun removeServiceTaskHandler(id: UUID, processDefinitionKey: String, serviceTaskId: String): ResponseEntity<RemoveServiceTaskHandlerResult> {
        val zaakTypeLinkId = ZaakTypeLinkId.existingId(id)

        val result = zaakTypeLinkService.removeServiceTaskHandler(zaakTypeLinkId, processDefinitionKey, serviceTaskId)
        return when (result.zaakTypeLink()) {
            null -> ResponseEntity.badRequest().body(result)
            else -> ok(result)
        }
    }
}