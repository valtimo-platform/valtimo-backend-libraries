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

import com.fasterxml.jackson.databind.ObjectMapper
import com.nhaarman.mockitokotlin2.whenever
import com.ritense.connector.domain.ConnectorInstance
import com.ritense.connector.domain.ConnectorInstanceId
import com.ritense.connector.domain.ConnectorProperties
import com.ritense.connector.domain.ConnectorType
import com.ritense.connector.service.ConnectorService
import com.ritense.document.service.DocumentService
import com.ritense.objectsapi.BaseTest
import com.ritense.objectsapi.domain.GenericObject
import com.ritense.objectsapi.domain.ObjectRecord
import com.ritense.objectsapi.domain.ProductAanvraag
import com.ritense.objectsapi.opennotificaties.OpenNotificatieConnector
import com.ritense.objectsapi.repository.AbonnementLinkRepository
import com.ritense.objectsapi.service.ObjectsApiConnector
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.springframework.core.ParameterizedTypeReference
import java.net.URI
import java.util.UUID


class ProductAanvraagConnectorTest : BaseTest() {
    lateinit var productAanvraagProperties: ProductAanvraagProperties
    lateinit var connectorService: ConnectorService
    lateinit var documentService: DocumentService
    lateinit var abonnementLinkRepository: AbonnementLinkRepository
    lateinit var objectsApiConnector: ObjectsApiConnector
    lateinit var openNotificatieConnector: OpenNotificatieConnector
    lateinit var productAanvraagConnector: ProductAanvraagConnector

    @BeforeEach
    fun setup() {
        productAanvraagProperties = ProductAanvraagProperties(
            openNotificatieConnectionName = "openNotificatieInstance",
            objectsApiConnectionName = "ObjectsApiInstance",
            typeMapping = listOf(
                ProductAanvraagTypeMapping(
                    "typeName", "related-case", "related-process"
                )
            ),
            aanvragerRolTypeUrl = "http://aanvragerroltype.url/zaken/api/v1/rollen/281fdae4-fc32-46e9-b621-bb4b444b8f52",
        )
        connectorService = mock(ConnectorService::class.java)
        documentService = mock(DocumentService::class.java)
        abonnementLinkRepository = mock(AbonnementLinkRepository::class.java)
        objectsApiConnector = mock(ObjectsApiConnector::class.java)
        openNotificatieConnector = mock(OpenNotificatieConnector::class.java)

        productAanvraagConnector = ProductAanvraagConnector(
            connectorService,
            productAanvraagProperties
        )
    }

    @Test
    fun `getProductAanvraag get productAanvraag from Objects API`() {
        val productAanvraagId = UUID.randomUUID();
        val productAanvraag = ProductAanvraag(
            "aanvraagType",
            ObjectMapper().createObjectNode(),
            emptyList(),
            URI.create("http://pdf.url"),
            "123456789"
        )
        val objectRecord = GenericObject(
            UUID.randomUUID(),
            "http://object.url",
            "type",
            ObjectRecord(productAanvraag)
        )
        whenever(connectorService.loadByName(any())).thenReturn(objectsApiConnector)
        `when`(objectsApiConnector.getTypedObject(productAanvraagId, typeReference<GenericObject<ProductAanvraag>>())).thenReturn(objectRecord)

        val resultProductAanvraag = productAanvraagConnector.getProductAanvraag(productAanvraagId)

        assertEquals(productAanvraag, resultProductAanvraag)
    }

    @Test
    fun `deleteProductAanvraag deletes object`() {
        whenever(connectorService.loadByName(any())).thenReturn(objectsApiConnector)
        val productAanvraagId = UUID.randomUUID();

        productAanvraagConnector.deleteProductAanvraag(productAanvraagId)

        verify(objectsApiConnector, times(1)).deleteObject(productAanvraagId)
    }


    @Test
    fun `getTypeMapping should filter out correct mapping`() {
        val mapping = productAanvraagConnector.getTypeMapping("typeName")

        assertEquals("related-case", mapping.caseDefinitionKey)
        assertEquals("related-process", mapping.processDefinitionKey)
    }

    @Test
    fun `getTypeMapping should throw NoSuchElementException when type is not found`() {
        Assertions.assertThrows(NoSuchElementException::class.java) {
            productAanvraagConnector.getTypeMapping("some-name")
        }
    }

    @Test
    fun `getAanvragerRolTypeUrl should return aanvragerRolTypeUrl from properties`() {
        val aanvragerRolTypeUrl = productAanvraagConnector.getAanvragerRolTypeUrl()
        assertEquals(URI("http://aanvragerroltype.url/zaken/api/v1/rollen/281fdae4-fc32-46e9-b621-bb4b444b8f52"), aanvragerRolTypeUrl)
    }

    @Test
    fun `onCreate should ensure kanaal exists and create abonnement`() {
        whenever(connectorService.loadByName(any())).thenReturn(openNotificatieConnector)
        val connectorId = ConnectorInstanceId.existingId(UUID.randomUUID());
        val connectorInstance = ConnectorInstance(
            connectorId,
            mock(ConnectorType::class.java),
            "test-connector",
            mock(ConnectorProperties::class.java)
        )

        productAanvraagConnector.onCreate(connectorInstance)

        verify(openNotificatieConnector, times(1)).ensureKanaalExists()
        verify(openNotificatieConnector, times(1)).createAbonnement(connectorId)
    }

    @Test
    fun onEdit() {
        whenever(connectorService.loadByName(any())).thenReturn(openNotificatieConnector)
        val connectorId = ConnectorInstanceId.existingId(UUID.randomUUID());
        val connectorInstance = ConnectorInstance(
            connectorId,
            mock(ConnectorType::class.java),
            "test-connector",
            mock(ConnectorProperties::class.java)
        )

        productAanvraagConnector.onEdit(connectorInstance)

        verify(openNotificatieConnector, times(1)).deleteAbonnement(connectorId)
        verify(openNotificatieConnector, times(1)).createAbonnement(connectorId)
    }

    @Test
    fun onDelete() {
        whenever(connectorService.loadByName(any())).thenReturn(openNotificatieConnector)
        val connectorId = ConnectorInstanceId.existingId(UUID.randomUUID());
        val connectorInstance = ConnectorInstance(
            connectorId,
            mock(ConnectorType::class.java),
            "test-connector",
            mock(ConnectorProperties::class.java)
        )

        productAanvraagConnector.onDelete(connectorInstance)

        verify(openNotificatieConnector, times(1)).deleteAbonnement(connectorId)
    }

    private inline fun <reified T> typeReference() = object : ParameterizedTypeReference<T>() {}
}