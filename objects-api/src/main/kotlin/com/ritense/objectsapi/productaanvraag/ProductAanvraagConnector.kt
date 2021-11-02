/*
 * Copyright 2015-2021 Ritense BV, the Netherlands.
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

package com.ritense.objectsapi.productaanvraag

import com.ritense.connector.domain.Connector
import com.ritense.connector.domain.ConnectorInstance
import com.ritense.connector.domain.ConnectorInstanceId
import com.ritense.connector.domain.ConnectorProperties
import com.ritense.connector.domain.meta.ConnectorType
import com.ritense.document.service.DocumentService
import com.ritense.objectsapi.domain.GenericObject
import com.ritense.objectsapi.domain.ProductAanvraag
import com.ritense.objectsapi.opennotificaties.OpenNotificatieClient
import com.ritense.objectsapi.repository.AbonnementLinkRepository
import com.ritense.objectsapi.opennotificaties.OpenNotificatieConnector
import com.ritense.objectsapi.service.ObjectsApiConnector
import java.net.URI
import java.util.UUID
import mu.KotlinLogging
import org.springframework.core.ParameterizedTypeReference

@ConnectorType(name = "ProductAanvragen")
class ProductAanvraagConnector(
    private var productAanvraagProperties: ProductAanvraagProperties,
    private var documentService: DocumentService,
    private var abonnementLinkRepository: AbonnementLinkRepository,
    private var objectsApiConnector: ObjectsApiConnector = ObjectsApiConnector(
        productAanvraagProperties.objectsApiProperties,
        documentService
    ),
    private var openNotificatieConnector: OpenNotificatieConnector = OpenNotificatieConnector(
        productAanvraagProperties.openNotificatieProperties,
        abonnementLinkRepository,
        OpenNotificatieClient(productAanvraagProperties.openNotificatieProperties)
    )
) : Connector {

    override fun getProperties(): ConnectorProperties {
        return productAanvraagProperties
    }

    override fun setProperties(connectorProperties: ConnectorProperties) {
        productAanvraagProperties = connectorProperties as ProductAanvraagProperties
        objectsApiConnector.setProperties(productAanvraagProperties.objectsApiProperties)
        openNotificatieConnector.setProperties(productAanvraagProperties.openNotificatieProperties)
    }

    fun getProductAanvraag(productAanvraagId: UUID): ProductAanvraag {
        return objectsApiConnector.getTypedObject(productAanvraagId, typeReference<GenericObject<ProductAanvraag>>()).record.data
    }

    fun deleteProductAanvraag(productAanvraagId: UUID) {
        objectsApiConnector.deleteObject(productAanvraagId)
    }

    fun getTypeMapping(type: String): ProductAanvraagTypeMapping {
        val typeMapping =
            productAanvraagProperties.typeMapping.filter { it.productAanvraagType.equals(type) }.firstOrNull()
        if (typeMapping == null) {
            logger.error { "Requested productaanvraag type mapping $type could not be found. " +
                "This should be set in the productaanvraag connnector properties" }
            throw NoSuchElementException("Requested productaanvraag type mapping $type could not be found")
        }
        return typeMapping
    }

    fun getAanvragerRolTypeUrl(): URI {
        return URI(productAanvraagProperties.aanvragerRolTypeUrl)
    }

    fun verifyKey(connectorId: UUID, key: String): Boolean {
        return openNotificatieConnector.verifyAbonnementKey(ConnectorInstanceId.existingId(connectorId), key)
    }

    override fun onCreate(connectorInstance: ConnectorInstance) {
        openNotificatieConnector.ensureKanaalExists()
        openNotificatieConnector.createAbonnement(connectorInstance.id)
    }

    override fun onEdit(connectorInstance: ConnectorInstance) {
        openNotificatieConnector.deleteAbonnement(connectorInstance.id)
        openNotificatieConnector.createAbonnement(connectorInstance.id)
    }

    override fun onDelete(connectorInstance: ConnectorInstance) {
        openNotificatieConnector.deleteAbonnement(connectorInstance.id)
    }

    private inline fun <reified T> typeReference() = object : ParameterizedTypeReference<T>() {}

    companion object {
        val logger = KotlinLogging.logger {}
    }
}