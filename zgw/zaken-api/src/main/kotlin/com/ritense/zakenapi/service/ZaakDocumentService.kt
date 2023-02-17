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

package com.ritense.zakenapi.service

import com.ritense.document.domain.RelatedFile
import com.ritense.documentenapi.DocumentenApiPlugin
import com.ritense.documentenapi.client.DocumentInformatieObject
import com.ritense.plugin.service.PluginService
import com.ritense.zakenapi.ZaakUrlProvider
import com.ritense.zakenapi.ZakenApiPlugin
import java.util.UUID

class ZaakDocumentService(
    val zaakUrlProvider: ZaakUrlProvider,
    val pluginService: PluginService
) {

    fun getFiles(documentId: UUID): List<DocumentInformatieObject> {
        val zaakUri = zaakUrlProvider.getZaakUrl(documentId)

        val zakenApiPlugin = checkNotNull(pluginService.createInstance(ZakenApiPlugin::class.java) { jsonNode ->
            zaakUri.toString().startsWith(jsonNode.get(ZakenApiPlugin.URL_PROPERTY).textValue())
        }) { "Could not find ${ZakenApiPlugin::class.simpleName} configuration for zaak with url: $zaakUri" }

        return zakenApiPlugin.getZaakInformatieObjecten(zaakUri).mapNotNull { zaakInformatieObject ->
            pluginService.createInstance(DocumentenApiPlugin::class.java) { jsonNode ->
                zaakInformatieObject.informatieobject.toString()
                    .startsWith(jsonNode.get(DocumentenApiPlugin.URL_PROPERTY).textValue())
            }?.getInformatieObject(zaakInformatieObject.informatieobject)
        }
    }

}