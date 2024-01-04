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

package com.ritense.documentenapi.service

import com.ritense.document.domain.RelatedFile
import com.ritense.documentenapi.DocumentenApiPlugin
import com.ritense.documentenapi.client.DocumentInformatieObject
import com.ritense.documentenapi.client.PatchDocumentRequest
import com.ritense.documentenapi.web.rest.dto.ModifyDocumentRequest
import com.ritense.plugin.service.PluginService
import org.springframework.transaction.annotation.Transactional
import java.io.InputStream

@Transactional
class DocumentenApiService(
    val pluginService: PluginService
) {
    fun downloadInformatieObject(pluginConfigurationId: String, documentId: String): InputStream {
        val documentApiPlugin: DocumentenApiPlugin = pluginService.createInstance(pluginConfigurationId)
        return documentApiPlugin.downloadInformatieObject(documentId)
    }

    fun getInformatieObject(pluginConfigurationId: String, documentId: String): DocumentInformatieObject {
        val documentApiPlugin: DocumentenApiPlugin = pluginService.createInstance(pluginConfigurationId)
        return documentApiPlugin.getInformatieObject(documentId)
    }

    fun modifyInformatieObject(
        pluginConfigurationId: String,
        documentId: String,
        modifyDocumentRequest: ModifyDocumentRequest
    ): RelatedFile? {
        val documentApiPlugin: DocumentenApiPlugin = pluginService.createInstance(pluginConfigurationId)
        val documentUrl = documentApiPlugin.createInformatieObjectUrl(documentId)
        documentApiPlugin.modifyInformatieObject(documentUrl, PatchDocumentRequest(modifyDocumentRequest))
    }

    fun deleteInformatieObject(pluginConfigurationId: String, documentId: String) {
        val documentApiPlugin: DocumentenApiPlugin = pluginService.createInstance(pluginConfigurationId)
        val documentUrl = documentApiPlugin.createInformatieObjectUrl(documentId)
        documentApiPlugin.deleteInformatieObject(documentUrl)
    }

}
