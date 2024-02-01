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

import com.ritense.document.domain.event.DocumentCreatedEvent
import com.ritense.plugin.service.PluginService
import com.ritense.zakenapi.ZakenApiPlugin
import org.springframework.context.event.EventListener
import org.springframework.core.annotation.Order

class DocumentCreatedListener(
    val pluginService: PluginService,
    val zaakTypeLinkService: ZaakTypeLinkService
) {
    @Order(0)
    @EventListener(DocumentCreatedEvent::class)
    fun handle(event: DocumentCreatedEvent) {
        val zaakTypeLink = zaakTypeLinkService.get(event.definitionId().name())
        zaakTypeLink?.let {
            if (it.createWithDossier && it.zakenApiPluginConfigurationId != null) {
                val zakenApiPlugin = pluginService.createInstance(it.zakenApiPluginConfigurationId!!) as ZakenApiPlugin
                zakenApiPlugin.createZaak(event.documentId().id, it.rsin!!, it.zaakTypeUrl)
            }
        }
    }
}