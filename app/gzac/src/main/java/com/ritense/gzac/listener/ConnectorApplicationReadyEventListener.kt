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

package com.ritense.gzac.listener

import com.ritense.besluit.connector.BesluitProperties
import com.ritense.connector.domain.ConnectorType
import com.ritense.connector.service.ConnectorService
import com.ritense.contactmoment.connector.ContactMomentProperties
import com.ritense.document.domain.event.DocumentDefinitionDeployedEvent
import com.ritense.haalcentraal.brp.connector.HaalCentraalBrpProperties
import com.ritense.objectsapi.opennotificaties.OpenNotificatieProperties
import com.ritense.objectsapi.productaanvraag.ProductAanvraagProperties
import com.ritense.objectsapi.productaanvraag.ProductAanvraagTypeMapping
import com.ritense.objectsapi.service.ObjectSyncService
import com.ritense.objectsapi.service.ObjectTypeConfig
import com.ritense.objectsapi.service.ObjectsApiProperties
import com.ritense.objectsapi.service.ServerAuthSpecification
import com.ritense.objectsapi.taak.TaakProperties
import com.ritense.objectsapi.web.rest.request.CreateObjectSyncConfigRequest
import com.ritense.openzaak.domain.configuration.Rsin
import com.ritense.openzaak.domain.connector.OpenZaakConfig
import com.ritense.openzaak.domain.connector.OpenZaakProperties
import com.ritense.openzaak.service.InformatieObjectTypeLinkService
import com.ritense.openzaak.web.rest.request.CreateInformatieObjectTypeLinkRequest
import mu.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.net.URI
import java.util.UUID

@ConditionalOnClass(
    name = [
        "com.ritense.besluit.domain.Besluit",
        "com.ritense.contactmoment.connector.ContactMomentProperties",
        "com.ritense.openzaak.domain.connector.OpenZaakConfig"
    ]
)
@Component
class ConnectorApplicationReadyEventListener(
    private val connectorService: ConnectorService,
    private val objectSyncService: ObjectSyncService,
    private val informatieObjectTypeLinkService: InformatieObjectTypeLinkService,
) {
    init {
        logger.info("ConnectorApplicationReadyEventListener created")
    }

    @EventListener(ApplicationReadyEvent::class)
    fun handleApplicationReady() {
        createConnectors()
    }

    @EventListener(DocumentDefinitionDeployedEvent::class)
    fun handleDocumentDefinitionDeployed(event: DocumentDefinitionDeployedEvent) {
        connectZaakType(event)
    }

    private fun createConnectors() {
        val connectorTypes = connectorService.getConnectorTypes()

        try {
            createHaalCentraalBrpConnector(connectorTypes.findId("HaalCentraalBrp"))
            createOpenZaakConnector(connectorTypes.findId("OpenZaak"))
            createOpenNotificatiesConnector(connectorTypes.findId("OpenNotificatie"))
            createContactMomentConnector(connectorTypes.findId("ContactMoment"))
            createObjectApiConnectors(connectorTypes.findId("ObjectsApi"))
            createProductAanvraagConnector(connectorTypes.findId("ProductAanvragen"))
            createTaakConnector(connectorTypes.findId("Taak"))
            createBesluitConnector(connectorTypes.findId("Besluiten"))
        } catch (ex: Exception) {
            logger.error { ex }
        }
    }

    private fun List<ConnectorType>.findId(connectorName: String): UUID {
        return this.first { it.name == connectorName }
            .id.id
    }

    private fun createHaalCentraalBrpConnector(id: UUID) {
        connectorService.createConnectorInstance(
            typeId = id,
            name = "HaalCentraalBrpInstance",
            connectorProperties = HaalCentraalBrpProperties(
                System.getenv("VALTIMO_HAALCENTRAAL_URL") ?: "http://example.com/",
                System.getenv("VALTIMO_HAALCENTRAAL_APIKEY") ?: "example-api-key"
            )
        )
    }

    private fun createOpenZaakConnector(id: UUID) {
        connectorService.createConnectorInstance(
            typeId = id,
            name = "OpenZaakInstance",
            connectorProperties = OpenZaakProperties(
                OpenZaakConfig(
                    "http://localhost:8001",
                    "valtimo_client",
                    CLIENT_ID,
                    Rsin("051845623"),
                    "http://localhost:4200/catalogi/api/v1/catalogussen/8225508a-6840-413e-acc9-6422af120db1"
                )
            )
        )
    }

    private fun createContactMomentConnector(id: UUID) {
        connectorService.createConnectorInstance(
            typeId = id,
            name = "ContactMomentInstance",
            connectorProperties = ContactMomentProperties(
                "http://localhost:8006",
                "valtimo_client",
                CLIENT_ID,
                "051845623"
            )
        )
    }

    private fun createObjectApiConnectors(id: UUID) {
        createStraatverlichtingConnector(id)
        createTaakObjectsApiConnector(id)
        createProductAanvraagObjectsApiConnector(id)
    }

    private fun createStraatverlichtingConnector(id: UUID) {
        val result = connectorService.createConnectorInstance(
            typeId = id,
            name = "ObjectsApiInstance",
            connectorProperties = ObjectsApiProperties(
                objectsApi = ServerAuthSpecification(
                    OBJECTEN_API_URL,
                    "cd63e158f3aca276ef284e3033d020a22899c728"
                ),
                objectsTypeApi = ServerAuthSpecification(
                    OBJECTTYPEN_API_URL,
                    "cd63e158f3aca276ef284e3033d020a22899c728"
                ),
                objectType = ObjectTypeConfig(
                    "straatverlichting",
                    "Straatverlichting",
                    "http://host.docker.internal:8011/api/v1/objecttypes/3a82fb7f-fc9b-4104-9804-993f639d6d0d",
                    "2"
                )
            )
        )
        if (result.errors().isEmpty()) {
            objectSyncService.createObjectSyncConfig(
                request = CreateObjectSyncConfigRequest(
                    connectorInstanceId = result.connectorTypeInstance()!!.id.id,
                    enabled = true,
                    documentDefinitionName = "leningen",
                    objectTypeId = UUID.fromString("3a82fb7f-fc9b-4104-9804-993f639d6d0d")
                )
            )
        }
    }

    private fun createTaakObjectsApiConnector(id: UUID) {
        connectorService.createConnectorInstance(
            typeId = id,
            name = TAAK_OBJECTAPI_CONNECTOR_NAME,
            connectorProperties = ObjectsApiProperties(
                objectsApi = ServerAuthSpecification(
                    OBJECTEN_API_URL,
                    "182c13e2209161852c53cef53a879f7a2f923430"
                ),
                objectsTypeApi = ServerAuthSpecification(
                    OBJECTTYPEN_API_URL,
                    "cd63e158f3aca276ef284e3033d020a22899c728"
                ),
                objectType = ObjectTypeConfig(
                    "taak",
                    "Objecttypen API: Taak",
                    "http://host.docker.internal:8011/api/v1/objecttypes/3e852115-277a-4570-873a-9a64be3aeb34",
                    "1"
                )
            )
        )
    }

    private fun createProductAanvraagObjectsApiConnector(id: UUID) {
        connectorService.createConnectorInstance(
            typeId = id,
            name = PRODUCTAANVRAAG_OBJECTAPI_CONNECTOR_NAME,
            connectorProperties = ObjectsApiProperties(
                objectsApi = ServerAuthSpecification(
                    OBJECTEN_API_URL,
                    "182c13e2209161852c53cef53a879f7a2f923430"
                ),
                objectsTypeApi = ServerAuthSpecification(
                    OBJECTTYPEN_API_URL,
                    "cd63e158f3aca276ef284e3033d020a22899c728"
                ),
                objectType = ObjectTypeConfig(
                    "productAanvraag",
                    "Objecttypen API: Productaanvraag",
                    "http://host.docker.internal:8011/api/v1/objecttypes/021f685e-9482-4620-b157-34cd4003da6b",
                    "1"
                )
            )
        )
    }

    private fun createTaakConnector(id: UUID) {
        connectorService.createConnectorInstance(
            typeId = id,
            name = "TaakConnector",
            connectorProperties = TaakProperties(
                openNotificatieConnectionName = OPENNOTIFICATIES_CONNECTOR_NAME,
                objectsApiConnectionName = TAAK_OBJECTAPI_CONNECTOR_NAME
            )
        )
    }

    private fun createProductAanvraagConnector(id: UUID) {
        connectorService.createConnectorInstance(
            typeId = id,
            name = "ProductAanvragen",
            connectorProperties = ProductAanvraagProperties(
                openNotificatieConnectionName = OPENNOTIFICATIES_CONNECTOR_NAME,
                objectsApiConnectionName = PRODUCTAANVRAAG_OBJECTAPI_CONNECTOR_NAME,
                typeMapping = listOf(
                    ProductAanvraagTypeMapping(
                        "lening",
                        "leningen",
                        "lening-aanvragen"
                    )
                ),
                aanvragerRolTypeUrl = "http://localhost:8001/catalogi/api/v1/roltypen/1c359a1b-c38d-47b8-bed5-994db88ead61"
            )
        )
    }

    private fun createOpenNotificatiesConnector(id: UUID) {
        connectorService.createConnectorInstance(
            typeId = id,
            name = OPENNOTIFICATIES_CONNECTOR_NAME,
            connectorProperties = OpenNotificatieProperties(
                "http://localhost:8002",
                "valtimo",
                "zZ!xRP&\$qTn4A9ETa^ZMKepDm^8egjPz",
                "http://host.docker.internal:8080"
            )
        )
    }

    private fun createBesluitConnector(id: UUID) {
        connectorService.createConnectorInstance(
            typeId = id,
            name = "BesluitInstance",
            connectorProperties = BesluitProperties(
                "http://localhost:8001",
                "valtimo_client",
                CLIENT_ID,
                Rsin("051845623")
            )
        )
    }

    private fun connectZaakType(event: DocumentDefinitionDeployedEvent) {
        if (event.documentDefinition().id().name() == "bezwaar") {
            informatieObjectTypeLinkService.create(
                CreateInformatieObjectTypeLinkRequest(
                    "bezwaar",
                    URI(ZAAKTYPE_URL),
                    URI("http://localhost:8001/catalogi/api/v1/informatieobjecttypen/efc332f2-be3b-4bad-9e3c-49a6219c92ad")
                )
            )
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
        private const val OPENNOTIFICATIES_CONNECTOR_NAME = "OpenNotificaties"
        private const val TAAK_OBJECTAPI_CONNECTOR_NAME = "TaakObjects"
        private const val PRODUCTAANVRAAG_OBJECTAPI_CONNECTOR_NAME = "ProductAanvraagObjects"
        private const val CLIENT_ID = "e09b8bc5-5831-4618-ab28-41411304309d"
        private const val OBJECTEN_API_URL = "http://localhost:8010"
        private const val OBJECTTYPEN_API_URL = "http://localhost:8011"
        private const val ZAAKTYPE_URL =
            "http://localhost:8001/catalogi/api/v1/zaaktypen/744ca059-f412-49d4-8963-5800e4afd486"
    }
}
