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

package com.ritense.openzaak.service

import com.ritense.openzaak.domain.mapping.impl.ZaakInstanceLink
import com.ritense.openzaak.domain.mapping.impl.ZaakTypeLink
import com.ritense.openzaak.domain.mapping.impl.ZaakTypeLinkId
import com.ritense.openzaak.domain.request.CreateZaakTypeLinkRequest
import com.ritense.openzaak.service.result.CreateServiceTaskHandlerResult
import com.ritense.openzaak.service.result.CreateZaakTypeLinkResult
import com.ritense.openzaak.service.result.ModifyServiceTaskHandlerResult
import com.ritense.openzaak.service.result.RemoveServiceTaskHandlerResult
import com.ritense.openzaak.web.rest.request.ServiceTaskHandlerRequest

interface ZaakTypeLinkService {

    fun get(documentDefinitionName: String): ZaakTypeLink?

    fun findBy(documentDefinitionName: String): ZaakTypeLink

    fun createZaakTypeLink(request: CreateZaakTypeLinkRequest): CreateZaakTypeLinkResult

    fun deleteZaakTypeLinkBy(documentDefinitionName: String)

    fun assignZaakInstance(id: ZaakTypeLinkId, zaakInstanceLink: ZaakInstanceLink): ZaakTypeLink

    fun assignServiceTaskHandler(zaakTypeLinkId: ZaakTypeLinkId, request: ServiceTaskHandlerRequest): CreateServiceTaskHandlerResult

    fun modifyServiceTaskHandler(zaakTypeLinkId: ZaakTypeLinkId, request: ServiceTaskHandlerRequest): ModifyServiceTaskHandlerResult

    fun modify(zaakTypeLink: ZaakTypeLink)

    fun removeServiceTaskHandler(zaakTypeLinkId: ZaakTypeLinkId, processDefinitionKey: String, serviceTaskId: String): RemoveServiceTaskHandlerResult

}