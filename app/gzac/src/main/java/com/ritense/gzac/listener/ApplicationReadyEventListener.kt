package com.ritense.gzac.listener

import com.fasterxml.jackson.core.json.JsonReadFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ritense.besluit.connector.BesluitProperties
import com.ritense.connector.domain.ConnectorType
import com.ritense.connector.service.ConnectorService
import com.ritense.contactmoment.connector.ContactMomentProperties
import com.ritense.document.domain.event.DocumentDefinitionDeployedEvent
import com.ritense.document.service.DocumentDefinitionService
import com.ritense.haalcentraal.brp.connector.HaalCentraalBrpProperties
import com.ritense.objectmanagement.domain.ObjectManagement
import com.ritense.objectmanagement.service.ObjectManagementService
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
import com.ritense.plugin.service.PluginConfigurationSearchParameters
import com.ritense.plugin.service.PluginService
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
    private val zaakTypeLinkService: ZaakTypeLinkService,
    private val informatieObjectTypeLinkService: InformatieObjectTypeLinkService,
    private val documentDefinitionService: DocumentDefinitionService,
    private val pluginService: PluginService,
    private val objectManagementService: ObjectManagementService,
) {

    @EventListener(ApplicationReadyEvent::class)
    fun handleApplicationReady() {
        createConnectors()
        createPlugins()
    }

    @EventListener(DocumentDefinitionDeployedEvent::class)
    fun handleDocumentDefinitionDeployed(event: DocumentDefinitionDeployedEvent) {
        connectZaakType(event)
        setDocumentDefinitionRole(event)
    }

    fun createConnectors() {
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

    fun createPlugins() {
        try {
            val zakenApiAuthenticationPluginId = createZakenApiAuthenticationPlugin()
            createCatalogiApiPlugin(zakenApiAuthenticationPluginId)
            val notificatiesApiAuthenticationPluginId = createNotificatiesApiAuthenticationPlugin()
            val notificatiesApiPluginId = createNotificatiesApiPlugin(notificatiesApiAuthenticationPluginId)
            val objectenApiAuthenticationPluginId = createObjectenApiAuthenticationPlugin()
            val objectenApiPluginId = createObjectenApiPlugin(objectenApiAuthenticationPluginId)
            val objecttypenApiPluginId = createObjecttypenApiPlugin(objectenApiAuthenticationPluginId)
            val bezwaarConfigurationId = createBezwaarObjectManagement(objecttypenApiPluginId, objectenApiPluginId)
            createBomenObjectManagement(objecttypenApiPluginId, objectenApiPluginId)
            createVerzoekPlugin(notificatiesApiPluginId, bezwaarConfigurationId)
            createSmartDocumentsPlugin()
        } catch (ex: Exception) {
            logger.error { ex }
        }
    }

    fun List<ConnectorType>.findId(connectorName: String): UUID {
        return this
            .filter { it.name.equals(connectorName) }
            .first()
            .id.id
    }

    fun createHaalCentraalBrpConnector(id: UUID) {
        connectorService.createConnectorInstance(
            typeId = id,
            name = "HaalCentraalBrpInstance",
            connectorProperties = HaalCentraalBrpProperties(
                System.getenv("VALTIMO_HAALCENTRAAL_URL") ?: "http://example.com/",
                System.getenv("VALTIMO_HAALCENTRAAL_APIKEY") ?: "example-api-key"
            )
        )
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
                    Rsin("051845623"),
                    "http://localhost:4200/catalogi/api/v1/catalogussen/8225508a-6840-413e-acc9-6422af120db1"
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
        if (result.errors().isEmpty()) {
            val configResult = objectSyncService.createObjectSyncConfig(
                request = CreateObjectSyncConfigRequest(
                    connectorInstanceId = result.connectorTypeInstance()!!.id.id,
                    enabled = true,
                    documentDefinitionName = "leningen",
                    objectTypeId = UUID.fromString("3a82fb7f-fc9b-4104-9804-993f639d6d0d")
                )
            )
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

    fun createBesluitConnector(id: UUID) {
        connectorService.createConnectorInstance(
            typeId = id,
            name = "BesluitInstance",
            connectorProperties = BesluitProperties(
                "http://localhost:8001",
                "valtimo_client",
                "e09b8bc5-5831-4618-ab28-41411304309d",
                Rsin("051845623")
            )
        )
    }

    private fun createZakenApiAuthenticationPlugin(): UUID {
        val existing = pluginService.getPluginConfigurations(
            PluginConfigurationSearchParameters(
                pluginConfigurationTitle = "OpenZaak Authentication",
                pluginDefinitionKey = "openzaak",
            )
        )
        return if (existing.isEmpty()) {
            pluginService.createPluginConfiguration(
                title = "OpenZaak Authentication",
                pluginDefinitionKey = "openzaak",
                properties = jacksonObjectMapper().readValue(
                    """
                    {
                        "clientId": "valtimo_client",
                        "clientSecret": "e09b8bc5-5831-4618-ab28-41411304309d"
                    }
                    """
                )
            ).id.id
        } else {
            existing[0].id.id
        }
    }

    private fun createNotificatiesApiAuthenticationPlugin(): UUID {
        val existing = pluginService.getPluginConfigurations(
            PluginConfigurationSearchParameters(
                pluginConfigurationTitle = "OpenNotificaties Authentication",
                pluginDefinitionKey = "notificatiesapiauthentication",
            )
        )
        val mapper = JsonMapper.builder()
            .enable(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER)
            .build();
        return if (existing.isEmpty()) {
            pluginService.createPluginConfiguration(
                title = "OpenNotificaties Authentication",
                pluginDefinitionKey = "notificatiesapiauthentication",
                properties = mapper.readValue(
                    """
                    {
                        "clientId": "valtimo",
                        "clientSecret": "zZ!xRP&\${'$'}qTn4A9ETa^ZMKepDm^8egjPz"
                    }
                    """
                )
            ).id.id
        } else {
            existing[0].id.id
        }
    }

    private fun createNotificatiesApiPlugin(authenticationPluginConfigurationId: UUID): UUID {
        val existing = pluginService.getPluginConfigurations(
            PluginConfigurationSearchParameters(
                pluginConfigurationTitle = "Notificaties API",
                pluginDefinitionKey = "notificatiesapi",
            )
        )
        return if (existing.isEmpty()) {
            return pluginService.createPluginConfiguration(
                title = "Notificaties API",
                pluginDefinitionKey = "notificatiesapi",
                properties = jacksonObjectMapper().readValue(
                    """
                    {
                        "url": "http://localhost:8002/api/v1/",
                        "authenticationPluginConfiguration": "$authenticationPluginConfigurationId"
                    }
                    """
                )
            ).id.id
        } else {
            existing[0].id.id
        }
    }

    private fun createObjectenApiAuthenticationPlugin(): UUID {
        val existing = pluginService.getPluginConfigurations(
            PluginConfigurationSearchParameters(
                pluginConfigurationTitle = "Objecten API Authentication",
                pluginDefinitionKey = "objecttokenauthentication",
            )
        )
        return if (existing.isEmpty()) {
            pluginService.createPluginConfiguration(
                title = "Objecten API Authentication",
                pluginDefinitionKey = "objecttokenauthentication",
                properties = jacksonObjectMapper().readValue(
                    """
                    {
                        "token": "182c13e2209161852c53cef53a879f7a2f923430"
                    }
                    """
                )
            ).id.id
        } else {
            existing[0].id.id
        }
    }

    private fun createObjectenApiPlugin(authenticationPluginConfigurationId: UUID): UUID {
        val existing = pluginService.getPluginConfigurations(
            PluginConfigurationSearchParameters(
                pluginConfigurationTitle = "Objecten API",
                pluginDefinitionKey = "objectenapi",
            )
        )
        return if (existing.isEmpty()) {
            return pluginService.createPluginConfiguration(
                title = "Objecten API",
                pluginDefinitionKey = "objectenapi",
                properties = jacksonObjectMapper().readValue(
                    """
                    {
                        "url": "http://localhost:8010/api/v2/",
                        "authenticationPluginConfiguration": "$authenticationPluginConfigurationId"
                    }
                    """
                )
            ).id.id
        } else {
            existing[0].id.id
        }
    }

    private fun createObjecttypenApiPlugin(authenticationPluginConfigurationId: UUID): UUID {
        val existing = pluginService.getPluginConfigurations(
            PluginConfigurationSearchParameters(
                pluginConfigurationTitle = "Objecttypen API",
                pluginDefinitionKey = "objecttypenapi",
            )
        )
        return if (existing.isEmpty()) {
            return pluginService.createPluginConfiguration(
                title = "Objecttypen API",
                pluginDefinitionKey = "objecttypenapi",
                properties = jacksonObjectMapper().readValue(
                    """
                    {
                        "url": "http://host.docker.internal:8011/api/v1/",
                        "authenticationPluginConfiguration": "$authenticationPluginConfigurationId"
                    }
                    """
                )
            ).id.id
        } else {
            existing[0].id.id
        }
    }

    private fun createCatalogiApiPlugin(authenticationPluginConfigurationId: UUID): UUID {
        val existing = pluginService.getPluginConfigurations(
            PluginConfigurationSearchParameters(
                pluginConfigurationTitle = "Catalogi API",
                pluginDefinitionKey = "catalogiapi",
            )
        )
        return if (existing.isEmpty()) {
            return pluginService.createPluginConfiguration(
                title = "Catalogi API",
                pluginDefinitionKey = "catalogiapi",
                properties = jacksonObjectMapper().readValue(
                    """
                    {
                        "url": "http://localhost:8001/catalogi/api/v1/",
                        "authenticationPluginConfiguration": "$authenticationPluginConfigurationId"
                    }
                    """
                )
            ).id.id
        } else {
            existing[0].id.id
        }
    }

    private fun createVerzoekPlugin(notificatiesApiPluginConfiguration: UUID, objectManagementId: UUID): UUID {
        val existing = pluginService.getPluginConfigurations(
            PluginConfigurationSearchParameters(
                pluginConfigurationTitle = "Verzoek lening",
                pluginDefinitionKey = "verzoek",
            )
        )
        return if (existing.isEmpty()) {
            return pluginService.createPluginConfiguration(
                title = "Verzoek lening",
                pluginDefinitionKey = "verzoek",
                properties = jacksonObjectMapper().readValue(
                    """
                    {
                        "notificatiesApiPluginConfiguration": "$notificatiesApiPluginConfiguration",
                        "objectManagementId": "$objectManagementId",
                        "systemProcessDefinitionKey": "verzoek",
                        "rsin": "051845623",
                        "verzoekProperties": [{
                            "type": "lening",
                            "caseDefinitionName": "leningen",
                            "processDefinitionKey": "lening-aanvragen",
                            "initiatorRoltypeUrl": "http://localhost:8001/catalogi/api/v1/roltypen/1c359a1b-c38d-47b8-bed5-994db88ead61",
                            "initiatorRolDescription": "Initiator"
                        }]
                    }
                    """
                )
            ).id.id
        } else {
            existing[0].id.id
        }
    }

    private fun createSmartDocumentsPlugin(): UUID {
        val existing = pluginService.getPluginConfigurations(
            PluginConfigurationSearchParameters(
                pluginConfigurationTitle = "SmartDocuments",
                pluginDefinitionKey = "smartdocuments",
            )
        )
        return if (existing.isEmpty()) {
            return pluginService.createPluginConfiguration(
                title = "SmartDocuments",
                pluginDefinitionKey = "smartdocuments",
                properties = jacksonObjectMapper().readValue(
                    """
                    {
                        "url": "https://example.com/",
                        "username": "test-user",
                        "password": "test-password"
                    }
                    """
                )
            ).id.id
        } else {
            existing[0].id.id
        }
    }

    private fun createBezwaarObjectManagement(
        objecttypenApiPluginConfigurationId: UUID,
        objectenApiPluginConfigurationId: UUID
    ): UUID {
        return objectManagementService.update(
            ObjectManagement(
                id = UUID.fromString("29400564-d25f-491c-abb2-afc42894ac9d"),
                title = "Bezwaar",
                objecttypenApiPluginConfigurationId = objecttypenApiPluginConfigurationId,
                objecttypeId = "021f685e-9482-4620-b157-34cd4003da6b",
                objectenApiPluginConfigurationId = objectenApiPluginConfigurationId,
                showInDataMenu = false,
                formDefinitionView = null,
                formDefinitionEdit = null,
            )
        ).id
    }

    private fun createBomenObjectManagement(
        objecttypenApiPluginConfigurationId: UUID,
        objectenApiPluginConfigurationId: UUID
    ): UUID {
        return objectManagementService.update(
            ObjectManagement(
                id = UUID.fromString("d8257077-ec44-44d3-9d2e-e8c87fa6fd09"),
                title = "Bomen",
                objecttypenApiPluginConfigurationId = objecttypenApiPluginConfigurationId,
                objecttypeId = "feeaa795-d212-4fa2-bb38-2c34996e5702",
                objectenApiPluginConfigurationId = objectenApiPluginConfigurationId,
                showInDataMenu = true,
                formDefinitionView = "boom.editform",
                formDefinitionEdit = "boom.editform",
            )
        ).id
    }

    private fun connectZaakType(event: DocumentDefinitionDeployedEvent) {
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

    private fun setDocumentDefinitionRole(event: DocumentDefinitionDeployedEvent) {
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
