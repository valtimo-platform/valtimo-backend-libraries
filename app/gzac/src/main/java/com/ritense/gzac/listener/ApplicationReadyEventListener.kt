package com.ritense.gzac.listener

import com.ritense.connector.domain.ConnectorType
import com.ritense.connector.service.ConnectorService
import com.ritense.contactmoment.connector.ContactMomentProperties
import com.ritense.document.domain.event.DocumentDefinitionDeployedEvent
import com.ritense.document.service.DocumentDefinitionService
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
import com.ritense.openzaak.domain.request.CreateZaakTypeLinkRequest
import com.ritense.openzaak.service.InformatieObjectTypeLinkService
import com.ritense.openzaak.service.ZaakTypeLinkService
import com.ritense.openzaak.web.rest.request.CreateInformatieObjectTypeLinkRequest
import com.ritense.processdocument.domain.impl.request.ProcessDocumentDefinitionRequest
import com.ritense.processdocument.service.ProcessDocumentAssociationService
import com.ritense.valtimo.contract.authentication.AuthoritiesConstants
import mu.KotlinLogging
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.net.URI
import java.util.UUID


@Component
class ApplicationReadyEventListener(
    private val connectorService: ConnectorService,
    private val objectSyncService: ObjectSyncService,
    private val processDocumentAssociationService: ProcessDocumentAssociationService,
    private val zaakTypeLinkService: ZaakTypeLinkService,
    private val informatieObjectTypeLinkService: InformatieObjectTypeLinkService,
    private val documentDefinitionService: DocumentDefinitionService,
) {

    @EventListener(ApplicationReadyEvent::class)
    fun handleApplicationReady() {
        createConnectors()
    }

    @EventListener(DocumentDefinitionDeployedEvent::class)
    fun handleDocumentDefinitionDeployed(event: DocumentDefinitionDeployedEvent) {
        linkProcess(event)
        connectZaakType(event)
        setDocumentDefinitionRole(event)
    }

    fun createConnectors() {
        val connectorTypes = connectorService.getConnectorTypes()

        connectorService.getConnectorTypes().forEach {
            try {
                createOpenZaakConnector(connectorTypes.findId("OpenZaak"))
                createOpenNotificatiesConnector(connectorTypes.findId("OpenNotificatie"))
                createContactMomentConnector(connectorTypes.findId("ContactMoment"))
                createObjectApiConnectors(connectorTypes.findId("ObjectsApi"))
                createProductAanvraagConnector(connectorTypes.findId("ProductAanvragen"))
                createTaakConnector(connectorTypes.findId("Taak"))
            } catch (ex: Exception) {
                logger.error { ex }
            }
        }
    }

    fun List<ConnectorType>.findId(connectorName: String): UUID {
        return this
            .filter { it.name.equals(connectorName) }
            .first()
            .id.id
    }

    fun createOpenZaakConnector(id: UUID) {
        connectorService.createConnectorInstance(
            typeId = id,
            name = "OpenZaakInstance",
            connectorProperties = OpenZaakProperties(
                OpenZaakConfig(
                    "http://localhost:8001",
                    "valtimo_client",
                    "e09b8bc5-5831-4618-ab28-41411304309d",
                    Rsin("051845623")
                )
            )
        )
    }

    fun createContactMomentConnector(id: UUID) {
        connectorService.createConnectorInstance(
            typeId = id,
            name = "ContactMomentInstance",
            connectorProperties = ContactMomentProperties(
                "http://localhost:8006",
                "valtimo_client",
                "e09b8bc5-5831-4618-ab28-41411304309d",
                "051845623"
            )
        )
    }

    fun createObjectApiConnectors(id: UUID) {
        createStraatverlichtingConnector(id)
        createTaakObjectsApiConnector(id)
        createProductAanvraagObjectsApiConnector(id)
    }

    fun createStraatverlichtingConnector(id: UUID) {
        val result = connectorService.createConnectorInstance(
            typeId = id,
            name = "ObjectsApiInstance",
            connectorProperties = ObjectsApiProperties(
                objectsApi = ServerAuthSpecification(
                    "http://localhost:8010",
                    "cd63e158f3aca276ef284e3033d020a22899c728"
                ),
                objectsTypeApi = ServerAuthSpecification(
                    "http://localhost:8011",
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
        val configResult = objectSyncService.createObjectSyncConfig(
            request = CreateObjectSyncConfigRequest(
                connectorInstanceId = result.connectorTypeInstance()!!.id.id,
                enabled = true,
                documentDefinitionName = "leningen",
                objectTypeId = UUID.fromString("3a82fb7f-fc9b-4104-9804-993f639d6d0d")
            )
        )
        if (configResult.errors().isNotEmpty()) {
            configResult.errors()
        }
    }

    fun createTaakObjectsApiConnector(id: UUID) {
        connectorService.createConnectorInstance(
            typeId = id,
            name = TAAK_OBJECTAPI_CONNECTOR_NAME,
            connectorProperties = ObjectsApiProperties(
                objectsApi = ServerAuthSpecification(
                    "http://localhost:8010",
                    "182c13e2209161852c53cef53a879f7a2f923430"
                ),
                objectsTypeApi = ServerAuthSpecification(
                    "http://localhost:8011",
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

    fun createProductAanvraagObjectsApiConnector(id: UUID) {
        connectorService.createConnectorInstance(
            typeId = id,
            name = PRODUCTAANVRAAG_OBJECTAPI_CONNECTOR_NAME,
            connectorProperties = ObjectsApiProperties(
                objectsApi = ServerAuthSpecification(
                    "http://localhost:8010",
                    "182c13e2209161852c53cef53a879f7a2f923430"
                ),
                objectsTypeApi = ServerAuthSpecification(
                    "http://localhost:8011",
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

    fun createTaakConnector(id: UUID) {
        connectorService.createConnectorInstance(
            typeId = id,
            name = "TaakConnector",
            connectorProperties = TaakProperties(
                openNotificatieConnectionName = OPENNOTIFICATIES_CONNECTOR_NAME,
                objectsApiConnectionName = TAAK_OBJECTAPI_CONNECTOR_NAME
            )
        )
    }

    fun createProductAanvraagConnector(id: UUID) {
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

    fun createOpenNotificatiesConnector(id: UUID) {
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

    fun linkProcess(event: DocumentDefinitionDeployedEvent) {
        if (event.documentDefinition().id().name().equals("leningen")) {
            val linkRequest = ProcessDocumentDefinitionRequest(
                "lening-aanvragen",
                "leningen",
                true,
                false
            )
            processDocumentAssociationService.createProcessDocumentDefinition(linkRequest)
        }
    }

    fun connectZaakType(event: DocumentDefinitionDeployedEvent) {
        if (event.documentDefinition().id().name().equals("leningen")) {
            zaakTypeLinkService.createZaakTypeLink(
                CreateZaakTypeLinkRequest(
                    "leningen",
                    URI("http://localhost:8001/catalogi/api/v1/zaaktypen/744ca059-f412-49d4-8963-5800e4afd486"),
                    true
                )
            )
            informatieObjectTypeLinkService.create(
                CreateInformatieObjectTypeLinkRequest(
                    "leningen",
                    URI("http://localhost:8001/catalogi/api/v1/zaaktypen/744ca059-f412-49d4-8963-5800e4afd486"),
                    URI("http://localhost:8001/catalogi/api/v1/informatieobjecttypen/efc332f2-be3b-4bad-9e3c-49a6219c92ad")
                )
            )
        }
    }

    fun setDocumentDefinitionRole(event: DocumentDefinitionDeployedEvent) {
        documentDefinitionService.putDocumentDefinitionRoles(
            event.documentDefinition().id().name(),
            setOf(AuthoritiesConstants.ADMIN, AuthoritiesConstants.USER)
        )
    }

    companion object {
        val logger = KotlinLogging.logger {}
        val OPENNOTIFICATIES_CONNECTOR_NAME = "OpenNotificaties"
        val TAAK_OBJECTAPI_CONNECTOR_NAME = "TaakObjects"
        val PRODUCTAANVRAAG_OBJECTAPI_CONNECTOR_NAME = "ProductAanvraagObjects"
    }
}