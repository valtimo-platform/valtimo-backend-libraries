/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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
package com.ritense.valtimo.smartdocuments.connector

import com.ritense.connector.domain.Connector
import com.ritense.connector.domain.ConnectorProperties
import com.ritense.connector.domain.meta.ConnectorType
import com.ritense.documentgeneration.domain.GeneratedDocument
import com.ritense.documentgeneration.domain.placeholders.TemplatePlaceholders
import com.ritense.documentgeneration.domain.templatedata.TemplateData
import com.ritense.documentgeneration.domain.templatedata.TemplateDataBlock
import com.ritense.valtimo.smartdocuments.client.SmartDocumentsClient
import com.ritense.valtimo.smartdocuments.domain.SmartDocumentsRequest
import org.springframework.http.MediaType

@ConnectorType(name = "SmartDocuments")
class SmartDocumentsConnector(
    private var smartDocumentsConnectorProperties: SmartDocumentsConnectorProperties,
    private val smartDocumentsClient: SmartDocumentsClient,
) : Connector {

    override fun getProperties(): ConnectorProperties {
        return smartDocumentsConnectorProperties
    }

    override fun setProperties(connectorProperties: ConnectorProperties) {
        smartDocumentsConnectorProperties = connectorProperties as SmartDocumentsConnectorProperties
        smartDocumentsClient.setProperties(smartDocumentsConnectorProperties)
    }

    fun getTemplatePlaceholders(templateName: String): TemplatePlaceholders {
        throw UnsupportedOperationException()
    }

    fun generateDocument(templateName: String, templateData: TemplateData): GeneratedDocument {
        smartDocumentsClient.generateDocument(
            SmartDocumentsRequest(
                templateDataToMap(templateData),
                ...
        )
        )
    }

    fun getDocumentMediaType(): MediaType {
        return MediaType.APPLICATION_PDF
    }

    private fun templateDataToMap(templateData: TemplateData): Map<String, String> {
        val customerData = mutableMapOf<String, String>()

        templateData.templateDataFields.forEach { customerData[it.name] = it.value.toString() }
        templateData.templateDataBlocks.forEach { templateDataBlockToMap(it, "", customerData) }
        return customerData
    }

    private fun templateDataBlockToMap(
        templateDataBlock: TemplateDataBlock,
        namePrefix: String,
        customerData: MutableMap<String, String>
    ): Map<String, String> {
        templateDataBlock.templateDataBlockItems.forEach { item ->
            val namePrefix2 = namePrefix + "_" + templateDataBlock.name
            item.templateDataFields.forEach { customerData[namePrefix2 + "_" + it.name] = it.value.toString() }
            item.templateDataBlocks.forEach {
                templateDataBlockToMap(it, namePrefix2, customerData)
            }
        }
        return customerData
    }

}