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

package com.ritense.gzac.listener

import com.fasterxml.jackson.core.json.JsonReadFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ritense.besluit.connector.BesluitProperties
import com.ritense.besluitenapi.BesluitenApiPlugin
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
import com.ritense.plugin.web.rest.request.PluginProcessLinkCreateDto
import com.ritense.processdocument.domain.impl.request.DocumentDefinitionProcessRequest
import com.ritense.processdocument.service.DocumentDefinitionProcessLinkService
import com.ritense.processlink.domain.ActivityTypeWithEventName
import com.ritense.processlink.service.ProcessLinkService
import com.ritense.valtimo.contract.authentication.AuthoritiesConstants
import mu.KotlinLogging
import org.camunda.bpm.engine.RepositoryService
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
    private val processLinkService: ProcessLinkService,
    private val objectManagementService: ObjectManagementService,
    private val repositoryService: RepositoryService,
    private val documentDefinitionProcessLinkService: DocumentDefinitionProcessLinkService,
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
            val zakenApiAuthenticationPluginId = UUID.fromString("b609a0a3-886e-4b3d-ae0d-c01effb311ee")
            val zakenApiPluginId = UUID.fromString("3079d6fe-42e3-4f8f-a9db-52ce2507b7ee")
            val documentenApiPluginId = UUID.fromString("5474fe57-532a-4050-8d89-32e62ca3e895")
            val notificatiesApiAuthenticationPluginId = UUID.fromString("df36cd33-d0dd-429a-a8ad-e16f307ac434")
            val notificatiesApiPluginId = UUID.fromString("bb1c601b-b257-497e-bab0-c21d339335d7")
            val objectenApiAuthenticationPluginId = UUID.fromString("21a006f9-7833-4cdf-a6b7-1927705dd543")
            val objectenApiPluginId = UUID.fromString("b6d83348-97e7-4660-bd35-2e5fcc9629b4")
            val objecttypenApiAuthenticationPluginId = UUID.fromString("acb0687a-075e-4435-923b-e6cb01d4d5db")
            val objecttypenApiPluginId = UUID.fromString("4021bb75-18c8-4ca5-8658-b9f9c728bba0")

            val bezwaarConfigurationId = UUID.fromString("29400564-d25f-491c-abb2-afc42894ac9d")//createBezwaarObjectManagement(objecttypenApiPluginId, objectenApiPluginId)
            val taakConfigurationId = UUID.fromString("16c69c86-0c5d-4d57-b4ac-0add8271a142")//createTaakObjectManagement(objecttypenApiPluginId, objectenApiPluginId)
            createBomenObjectManagement(objecttypenApiPluginId, objectenApiPluginId)

            //createVerzoekPlugin(notificatiesApiPluginId, bezwaarConfigurationId)

            val portaaltaakPluginId = UUID.fromString("d65113e0-a9cb-4904-93e1-5e8b1206e625")//createPortaaltaakPlugin(notificatiesApiPluginId, taakConfigurationId)

            portalPersonCreatePortaaltaak(portaaltaakPluginId)
            processCompletedPortaalTaakCompletePortaaltaak(portaaltaakPluginId)
            processCompletedPortaalTaakLinkDocumentToZaak(zakenApiPluginId)
            createZaakdossierCreateZaak(zakenApiPluginId)
            createZaakdossierCreateZaakRol(zakenApiPluginId)
            createZaakdossierLinkDocumentToZaak(zakenApiPluginId)
            createZaakdossierDeleteVerzoek(objectenApiPluginId)
            uploadDocumentUploadDocument(documentenApiPluginId)
            uploadDocumentLinkDocumentToZaak(zakenApiPluginId)
        } catch (ex: Exception) {
            throw RuntimeException("Failed to deploy plugin configurations for development", ex)
        }
    }

    private fun portalPersonCreatePortaaltaak(portaaltaakPluginId: UUID) {
        createProcessLinkIfNotExists(
            processDefinitionKey = "portal-person",
            activityId = "portal-task",
            activityType = "bpmn:UserTask:create",
            pluginConfigurationId = portaaltaakPluginId,
            pluginActionDefinitionKey = "create-portaaltaak",
            actionProperties = """
                {
                    "formType": "id",
                    "formTypeId": "person",
                    "sendData": [
                        {
                            "key": "/firstName",
                            "value": "doc:/firstName"
                        }
                    ],
                    "receiveData": [
                        {
                            "key": "doc:/firstName",
                            "value": "/firstName"
                        }
                    ],
                    "receiver": "other",
                    "identificationKey": "bsn",
                    "identificationValue": "569312863"
                }
                """.trimIndent()
        )
    }

    private fun processCompletedPortaalTaakCompletePortaaltaak(portaaltaakPluginId: UUID) {
        createProcessLinkIfNotExists(
            processDefinitionKey = "process-completed-portaaltaak",
            activityId = "update_portaal_taak_status",
            activityType = "bpmn:ServiceTask:start",
            pluginConfigurationId = portaaltaakPluginId,
            pluginActionDefinitionKey = "complete-portaaltaak",
            actionProperties = "{}",
        )
    }

    private fun processCompletedPortaalTaakLinkDocumentToZaak(zakenApiPluginId: UUID) {
        createProcessLinkIfNotExists(
            processDefinitionKey = "process-completed-portaaltaak",
            activityId = "link-document-to-zaak",
            activityType = "bpmn:ServiceTask:start",
            pluginConfigurationId = zakenApiPluginId,
            pluginActionDefinitionKey = "link-document-to-zaak",
            actionProperties = """
                {
                    "documentUrl": "pv:documentUrl",
                    "titel": "Portal document",
                    "beschrijving": "This document was uploaded in the portal"
                }
                """.trimIndent()
        )
    }

    private fun createZaakdossierCreateZaak(zakenApiPluginId: UUID) {
        createProcessLinkIfNotExists(
            processDefinitionKey = "create-zaakdossier",
            activityId = "create-zaak",
            activityType = "bpmn:ServiceTask:start",
            pluginConfigurationId = zakenApiPluginId,
            pluginActionDefinitionKey = "create-zaak",
            actionProperties = """
                {
                    "rsin": "051845623",
                    "zaaktypeUrl": "http://localhost:8001/catalogi/api/v1/zaaktypen/744ca059-f412-49d4-8963-5800e4afd486"
                }
            """.trimIndent()
        )
    }

    private fun createZaakdossierCreateZaakRol(zakenApiPluginId: UUID) {
        createProcessLinkIfNotExists(
            processDefinitionKey = "create-zaakdossier",
            activityId = "create-initiator-zaak-rol",
            activityType = "bpmn:ServiceTask:start",
            pluginConfigurationId = zakenApiPluginId,
            pluginActionDefinitionKey = "create-natuurlijk-persoon-zaak-rol",
            actionProperties = """
                {
                    "roltypeUrl": "pv:rolTypeUrl",
                    "rolToelichting": "pv:rolDescription",
                    "inpBsn": "pv:initiatorValue"
                }
            """.trimIndent()
        )
    }

    private fun createZaakdossierLinkDocumentToZaak(zakenApiPluginId: UUID) {
        createProcessLinkIfNotExists(
            processDefinitionKey = "create-zaakdossier",
            activityId = "link-document-to-zaak",
            activityType = "bpmn:ServiceTask:start",
            pluginConfigurationId = zakenApiPluginId,
            pluginActionDefinitionKey = "link-document-to-zaak",
            actionProperties = """
                {
                    "documentUrl": "pv:documentUrl",
                    "titel": "Verzoek document",
                    "beschrijving": "Document that belongs to a Verzoek"
                }
            """.trimIndent(),
        )
    }

    private fun createZaakdossierDeleteVerzoek(objectenApiPluginId: UUID) {
        createProcessLinkIfNotExists(
            processDefinitionKey = "create-zaakdossier",
            activityId = "delete-verzoek-from-objectsapi",
            activityType = "bpmn:ServiceTask:start",
            pluginConfigurationId = objectenApiPluginId,
            pluginActionDefinitionKey = "delete-object",
            actionProperties = """
                {
                    "objectUrl": "pv:verzoekObjectUrl"
                }
            """.trimIndent(),
        )
    }

    private fun uploadDocumentUploadDocument(documentenApiPluginId: UUID) {
        createProcessLinkIfNotExists(
            processDefinitionKey = "document-upload",
            activityId = "upload-document",
            activityType = "bpmn:ServiceTask:start",
            pluginConfigurationId = documentenApiPluginId,
            pluginActionDefinitionKey = "store-uploaded-document",
            actionProperties = "{}",
        )
    }

    private fun uploadDocumentLinkDocumentToZaak(zakenApiPluginId: UUID) {
        createProcessLinkIfNotExists(
            processDefinitionKey = "document-upload",
            activityId = "link-document-to-zaak",
            activityType = "bpmn:ServiceTask:start",
            pluginConfigurationId = zakenApiPluginId,
            pluginActionDefinitionKey = "link-uploaded-document-to-zaak",
            actionProperties = "{}",
        )
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
        logger.debug { "Creating OpenZaak Authentication plugin" }
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
        logger.debug { "Creating OpenNotificaties Authentication plugin" }
        val existing = pluginService.getPluginConfigurations(
            PluginConfigurationSearchParameters(
                pluginConfigurationTitle = "OpenNotificaties Authentication",
                pluginDefinitionKey = "notificatiesapiauthentication",
            )
        )
        val mapper = JsonMapper.builder()
            .enable(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER)
            .build()
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
        logger.debug { "Creating Notificaties API plugin" }
        val existing = pluginService.getPluginConfigurations(
            PluginConfigurationSearchParameters(
                pluginConfigurationTitle = "Notificaties API",
                pluginDefinitionKey = "notificatiesapi",
            )
        )
        return if (existing.isEmpty()) {
            pluginService.createPluginConfiguration(
                title = "Notificaties API",
                pluginDefinitionKey = "notificatiesapi",
                properties = jacksonObjectMapper().readValue(
                    """
                    {
                        "url": "http://localhost:8002/api/v1/",
                        "callbackUrl": "http://host.docker.internal:8080/api/v1/notificatiesapi/callback",
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
        logger.debug { "Creating Objecten API Authentication plugin" }
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

    private fun createObjecttypenApiAuthenticationPlugin(): UUID {
        logger.debug { "Creating Objecttypen API Authentication plugin" }
        val existing = pluginService.getPluginConfigurations(
            PluginConfigurationSearchParameters(
                pluginConfigurationTitle = "Objecttypen API Authentication",
                pluginDefinitionKey = "objecttokenauthentication",
            )
        )
        return if (existing.isEmpty()) {
            pluginService.createPluginConfiguration(
                title = "Objecttypen API Authentication",
                pluginDefinitionKey = "objecttokenauthentication",
                properties = jacksonObjectMapper().readValue(
                    """
                    {
                        "token": "cd63e158f3aca276ef284e3033d020a22899c728"
                    }
                    """
                )
            ).id.id
        } else {
            existing[0].id.id
        }
    }

    private fun createObjectenApiPlugin(authenticationPluginConfigurationId: UUID): UUID {
        logger.debug { "Creating Objecten API plugin" }
        val existing = pluginService.getPluginConfigurations(
            PluginConfigurationSearchParameters(
                pluginConfigurationTitle = "Objecten API",
                pluginDefinitionKey = "objectenapi",
            )
        )
        return if (existing.isEmpty()) {
            pluginService.createPluginConfiguration(
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
        logger.debug { "Creating Objecttypen API plugin" }
        val existing = pluginService.getPluginConfigurations(
            PluginConfigurationSearchParameters(
                pluginConfigurationTitle = "Objecttypen API",
                pluginDefinitionKey = "objecttypenapi",
            )
        )
        return if (existing.isEmpty()) {
            pluginService.createPluginConfiguration(
                title = "Objecttypen API",
                pluginDefinitionKey = "objecttypenapi",
                properties = jacksonObjectMapper().readValue(
                    """
                    {
                        "url": "http://localhost:8011/api/v1/",
                        "authenticationPluginConfiguration": "$authenticationPluginConfigurationId"
                    }
                    """
                )
            ).id.id
        } else {
            existing[0].id.id
        }
    }

    private fun createZakenApiPlugin(authenticationPluginConfigurationId: UUID): UUID {
        logger.debug { "Creating Zaken API plugin" }
        val existing = pluginService.getPluginConfigurations(
            PluginConfigurationSearchParameters(
                pluginConfigurationTitle = "Zaken API",
                pluginDefinitionKey = "zakenapi",
            )
        )
        return if (existing.isEmpty()) {
            pluginService.createPluginConfiguration(
                title = "Zaken API",
                pluginDefinitionKey = "zakenapi",
                properties = jacksonObjectMapper().readValue(
                    """
                    {
                        "url": "http://localhost:8001/zaken/api/v1/",
                        "authenticationPluginConfiguration": "$authenticationPluginConfigurationId"
                    }
                    """
                )
            ).id.id
        } else {
            existing[0].id.id
        }
    }

    private fun createBesluitenApiPlugin(authenticationPluginConfigurationId: UUID): UUID {
        val title = "Besluiten API"
        logger.debug { "Creating $title plugin" }
        val existing = pluginService.getPluginConfigurations(
            PluginConfigurationSearchParameters(
                pluginConfigurationTitle = title,
                pluginDefinitionKey = BesluitenApiPlugin.PLUGIN_KEY,
            )
        )
        return if (existing.isEmpty()) {
            pluginService.createPluginConfiguration(
                title = title,
                pluginDefinitionKey = BesluitenApiPlugin.PLUGIN_KEY,
                properties = jacksonObjectMapper().readValue(
                    """
                    {
                        "url": "http://localhost:8001/besluiten/api/v1/",
                        "rsin": "051845623",
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
        logger.debug { "Creating Catalogi API plugin" }
        val existing = pluginService.getPluginConfigurations(
            PluginConfigurationSearchParameters(
                pluginConfigurationTitle = "Catalogi API",
                pluginDefinitionKey = "catalogiapi",
            )
        )
        return if (existing.isEmpty()) {
            pluginService.createPluginConfiguration(
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

    private fun createDocumentenApiPlugin(authenticationPluginConfigurationId: UUID): UUID {
        logger.debug { "Creating Documenten API plugin" }
        val existing = pluginService.getPluginConfigurations(
            PluginConfigurationSearchParameters(
                pluginConfigurationTitle = "Documenten API",
                pluginDefinitionKey = "documentenapi",
            )
        )
        return if (existing.isEmpty()) {
            pluginService.createPluginConfiguration(
                title = "Documenten API",
                pluginDefinitionKey = "documentenapi",
                properties = jacksonObjectMapper().readValue(
                    """
                    {
                        "url": "http://localhost:8001/documenten/api/v1/",
                        "bronorganisatie": "051845623",
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
        logger.debug { "Creating Verzoek bezwaar plugin" }
        val existing = pluginService.getPluginConfigurations(
            PluginConfigurationSearchParameters(
                pluginConfigurationTitle = "Verzoek bezwaar",
                pluginDefinitionKey = "verzoek",
            )
        )
        return if (existing.isEmpty()) {
            pluginService.createPluginConfiguration(
                title = "Verzoek bezwaar",
                pluginDefinitionKey = "verzoek",
                properties = jacksonObjectMapper().readValue(
                    """
                    {
                        "notificatiesApiPluginConfiguration": "$notificatiesApiPluginConfiguration",
                        "objectManagementId": "$objectManagementId",
                        "processToStart": "create-zaakdossier",
                        "rsin": "051845623",
                        "verzoekProperties": [{
                            "type": "bezwaar",
                            "caseDefinitionName": "bezwaar",
                            "processDefinitionKey": "bezwaar",
                            "initiatorRoltypeUrl": "http://localhost:8001/catalogi/api/v1/roltypen/1c359a1b-c38d-47b8-bed5-994db88ead61",
                            "initiatorRolDescription": "Initiator",
                            "copyStrategy": "full"
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
        logger.debug { "Creating SmartDocuments plugin" }
        val existing = pluginService.getPluginConfigurations(
            PluginConfigurationSearchParameters(
                pluginConfigurationTitle = "SmartDocuments",
                pluginDefinitionKey = "smartdocuments",
            )
        )
        return if (existing.isEmpty()) {
            pluginService.createPluginConfiguration(
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

    private fun createTaakObjectManagement(
        objecttypenApiPluginConfigurationId: UUID,
        objectenApiPluginConfigurationId: UUID
    ): UUID {
        return objectManagementService.update(
            ObjectManagement(
                id = UUID.fromString("16c69c86-0c5d-4d57-b4ac-0add8271a142"),
                title = "Taak",
                objecttypenApiPluginConfigurationId = objecttypenApiPluginConfigurationId,
                objecttypeId = "3e852115-277a-4570-873a-9a64be3aeb34",
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
                objecttypeVersion = 2,
                objectenApiPluginConfigurationId = objectenApiPluginConfigurationId,
                showInDataMenu = true,
                formDefinitionView = "boom.summary",
                formDefinitionEdit = "boom.editform",
            )
        ).id
    }

    private fun connectZaakType(event: DocumentDefinitionDeployedEvent) {
        if (event.documentDefinition().id().name().equals("bezwaar")) {
            zaakTypeLinkService.createZaakTypeLink(
                CreateZaakTypeLinkRequest(
                    "bezwaar",
                    URI("http://localhost:8001/catalogi/api/v1/zaaktypen/744ca059-f412-49d4-8963-5800e4afd486"),
                    true
                )
            )
            informatieObjectTypeLinkService.create(
                CreateInformatieObjectTypeLinkRequest(
                    "bezwaar",
                    URI("http://localhost:8001/catalogi/api/v1/zaaktypen/744ca059-f412-49d4-8963-5800e4afd486"),
                    URI("http://localhost:8001/catalogi/api/v1/informatieobjecttypen/efc332f2-be3b-4bad-9e3c-49a6219c92ad")
                )
            )
        }
        if (event.documentDefinition().id().name().equals("portal-person")) {
            zaakTypeLinkService.createZaakTypeLink(
                CreateZaakTypeLinkRequest(
                    "portal-person",
                    URI("http://localhost:8001/catalogi/api/v1/zaaktypen/744ca059-f412-49d4-8963-5800e4afd486"),
                    true
                )
            )
        }
        documentDefinitionProcessLinkService.saveDocumentDefinitionProcess(
            "portal-person",
            DocumentDefinitionProcessRequest("document-upload", "DOCUMENT_UPLOAD")
        )
        documentDefinitionProcessLinkService.saveDocumentDefinitionProcess(
            "bezwaar",
            DocumentDefinitionProcessRequest("document-upload", "DOCUMENT_UPLOAD")
        )
    }

    private fun createPortaaltaakPlugin(
        notificatiesApiPluginConfigurationId: UUID,
        objectManagementConfigurationId: UUID
    ): UUID {
        val existing = pluginService.getPluginConfigurations(
            PluginConfigurationSearchParameters(
                pluginConfigurationTitle = "Portaaltaak",
                pluginDefinitionKey = "portaaltaak",
            )
        )
        return if (existing.isEmpty()) {
            pluginService.createPluginConfiguration(
                title = "Portaaltaak",
                pluginDefinitionKey = "portaaltaak",
                properties = jacksonObjectMapper().readValue(
                    """
                    {
                        "notificatiesApiPluginConfiguration": "$notificatiesApiPluginConfigurationId",
                        "objectManagementConfigurationId": "$objectManagementConfigurationId",
                        "completeTaakProcess": "process-completed-portaaltaak"
                    }
                    """
                )
            ).id.id
        } else {
            existing[0].id.id
        }
    }

    private fun setDocumentDefinitionRole(event: DocumentDefinitionDeployedEvent) {
        documentDefinitionService.putDocumentDefinitionRoles(
            event.documentDefinition().id().name(),
            setOf(AuthoritiesConstants.ADMIN, AuthoritiesConstants.USER)
        )
    }

    private fun createProcessLinkIfNotExists(
        processDefinitionKey: String,
        activityId: String,
        activityType: String,
        pluginConfigurationId: UUID,
        pluginActionDefinitionKey: String,
        actionProperties: String,
    ) {
        val processDefinitionId = repositoryService.createProcessDefinitionQuery()
            .processDefinitionKey(processDefinitionKey)
            .latestVersion()
            .singleResult()
            .id
        if (processLinkService.getProcessLinks(processDefinitionId, activityId).isEmpty()) {
            processLinkService.createProcessLink(
                PluginProcessLinkCreateDto(
                    processDefinitionId = processDefinitionId,
                    activityId = activityId,
                    pluginConfigurationId = pluginConfigurationId,
                    pluginActionDefinitionKey = pluginActionDefinitionKey,
                    actionProperties = jacksonObjectMapper().readValue(actionProperties),
                    activityType = ActivityTypeWithEventName.fromValue(activityType),
                )
            )
        }
    }

    companion object {
        val logger = KotlinLogging.logger {}
        val OPENNOTIFICATIES_CONNECTOR_NAME = "OpenNotificaties"
        val TAAK_OBJECTAPI_CONNECTOR_NAME = "TaakObjects"
        val PRODUCTAANVRAAG_OBJECTAPI_CONNECTOR_NAME = "ProductAanvraagObjects"
    }
}
