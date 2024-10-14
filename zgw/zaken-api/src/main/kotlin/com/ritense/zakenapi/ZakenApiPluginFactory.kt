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

package com.ritense.zakenapi

import com.ritense.document.service.DocumentService
import com.ritense.plugin.PluginFactory
import com.ritense.plugin.service.PluginService
import com.ritense.processdocument.service.ProcessDocumentAssociationService
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.resource.service.TemporaryResourceStorageService
import com.ritense.zakenapi.client.ZakenApiClient
import com.ritense.zakenapi.repository.ZaakHersteltermijnRepository
import com.ritense.zakenapi.repository.ZaakInstanceLinkRepository
import org.springframework.transaction.PlatformTransactionManager

class ZakenApiPluginFactory(
    pluginService: PluginService,
    private val client: ZakenApiClient,
    private val zaakUrlProvider: ZaakUrlProvider,
    private val storageService: TemporaryResourceStorageService,
    private val zaakInstanceLinkRepository: ZaakInstanceLinkRepository,
    private val zaakHersteltermijnRepository: ZaakHersteltermijnRepository,
    private val platformTransactionManager: PlatformTransactionManager,
    private val documentService: DocumentService,
    private val processDocumentAssociationService: ProcessDocumentAssociationService,
) : PluginFactory<ZakenApiPlugin>(pluginService) {

    override fun create(): ZakenApiPlugin {
        return ZakenApiPlugin(
            client,
            zaakUrlProvider,
            storageService,
            zaakInstanceLinkRepository,
            pluginService,
            zaakHersteltermijnRepository,
            platformTransactionManager,
            documentService,
            processDocumentAssociationService,
        )
    }
}
