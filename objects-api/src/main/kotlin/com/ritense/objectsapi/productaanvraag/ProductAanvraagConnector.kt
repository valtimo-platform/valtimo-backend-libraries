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

package com.ritense.objectsapi.productaanvraag

import com.ritense.connector.domain.Connector
import com.ritense.connector.domain.ConnectorInstance
import com.ritense.connector.domain.ConnectorProperties
import com.ritense.connector.domain.meta.ConnectorType
import com.ritense.connector.service.ConnectorService
import com.ritense.objectsapi.domain.GenericObject
import com.ritense.objectsapi.domain.ProductAanvraag
import com.ritense.objectsapi.opennotificaties.OpenNotificatieConnector
import com.ritense.objectsapi.productaanvraag.ProductAanvraagConnector.Companion.PRODUCT_AANVRAGEN_CONNECTOR_NAME
import com.ritense.objectsapi.service.ObjectsApiConnector
import java.net.URI
import java.util.UUID
import mu.KotlinLogging

@ConnectorType(name = PRODUCT_AANVRAGEN_CONNECTOR_NAME)
class ProductAanvraagConnector(
    private var connectorService: ConnectorService,
    private var productAanvraagProperties: ProductAanvraagProperties,
) : Connector {

    override fun getProperties(): ConnectorProperties {
        return productAanvraagProperties
    }

    override fun setProperties(connectorProperties: ConnectorProperties) {
        productAanvraagProperties = connectorProperties as ProductAanvraagProperties
    }

    fun getProductAanvraag(productAanvraagId: UUID): ProductAanvraag {
        val type = ObjectsApiConnector.typeReference<GenericObject<ProductAanvraag>>()
        return getObjectsApiConnector().getTypedObject(productAanvraagId, type).record.data
    }

    fun deleteProductAanvraag(productAanvraagId: UUID) {
        getObjectsApiConnector().deleteObject(productAanvraagId)
    }

    fun getTypeMapping(type: String): ProductAanvraagTypeMapping {
        val typeMapping =
            productAanvraagProperties.typeMapping.firstOrNull { it.productAanvraagType == type }
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

    fun getOpenNotificatieConnector(): OpenNotificatieConnector {
        return connectorService.loadByName(productAanvraagProperties.openNotificatieConnectionName) as OpenNotificatieConnector
    }

    fun getObjectsApiConnector(): ObjectsApiConnector {
        return connectorService.loadByName(productAanvraagProperties.objectsApiConnectionName) as ObjectsApiConnector
    }

    override fun onCreate(connectorInstance: ConnectorInstance) {
        val openNotificatieConnector = getOpenNotificatieConnector()
        openNotificatieConnector.ensureKanaalExists()
        openNotificatieConnector.createAbonnement(connectorInstance.id)
    }

    override fun onEdit(connectorInstance: ConnectorInstance) {
        val openNotificatieConnector = getOpenNotificatieConnector()
        openNotificatieConnector.deleteAbonnement(connectorInstance.id)
        openNotificatieConnector.createAbonnement(connectorInstance.id)
    }

    override fun onDelete(connectorInstance: ConnectorInstance) {
        getOpenNotificatieConnector().deleteAbonnement(connectorInstance.id)
    }

    companion object {
        val logger = KotlinLogging.logger {}
        const val PRODUCT_AANVRAGEN_CONNECTOR_NAME = "ProductAanvragen"
    }
}