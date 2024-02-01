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

package com.ritense.zakenapi.service

import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.zakenapi.domain.ZaakTypeLink
import com.ritense.zakenapi.web.rest.request.CreateZaakTypeLinkRequest

interface ZaakTypeLinkService {

    fun get(documentDefinitionName: String): ZaakTypeLink?

    fun getByPluginConfigurationId(id: PluginConfigurationId): List<ZaakTypeLink>

    fun getByProcess(processDefinitionKey: String): List<ZaakTypeLink>

    fun createZaakTypeLink(request: CreateZaakTypeLinkRequest): ZaakTypeLink

    fun deleteZaakTypeLinkBy(documentDefinitionName: String)

    fun modify(zaakTypeLink: ZaakTypeLink)
}
