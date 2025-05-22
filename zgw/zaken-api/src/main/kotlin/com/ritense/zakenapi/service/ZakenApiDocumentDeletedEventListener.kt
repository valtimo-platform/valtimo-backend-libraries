/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.logging.withLoggingContext
import com.ritense.plugin.service.PluginService
import com.ritense.valtimo.contract.annotation.AllOpen
import com.ritense.valtimo.contract.event.DocumentDeletedEvent
import com.ritense.zakenapi.ZakenApiPlugin
import com.ritense.zakenapi.link.ZaakInstanceLinkService
import mu.KLogger
import mu.KotlinLogging
import org.springframework.context.event.EventListener
import org.springframework.transaction.annotation.Transactional

@AllOpen
class ZakenApiDocumentDeletedEventListener(
    private val zaakInstanceService: ZaakInstanceLinkService,
    private val zaakDocumentService: ZaakDocumentService,
    private val pluginService: PluginService
) {
    @Transactional
    @EventListener(DocumentDeletedEvent::class)
    fun handle(event: DocumentDeletedEvent) {
        withLoggingContext(JsonSchemaDocument::class, event.documentId) {
            val link = try {
                zaakInstanceService.getByDocumentId(event.documentId)
            } catch (e: Exception) {
                logger.debug { "No zaak instance link found for document '${event.documentId}'. Not deleting any zaak information" }
                null
            }

            link?.let {
                logger.info { "Deleting all zaak information for deleted document ${event.documentId}" }
                //delete documents
                zaakDocumentService.deleteRelatedInformatieObjecten(link.zaakInstanceUrl)

                //delete zaak
                val plugin = pluginService.createInstance(
                    ZakenApiPlugin::class.java,
                    ZakenApiPlugin.findConfigurationByUrl(link.zaakInstanceUrl)
                )
                plugin?.deleteZaak(link.zaakInstanceUrl)
            }

        }
    }

    companion object {
        private val logger: KLogger = KotlinLogging.logger {}
    }
}