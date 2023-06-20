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

import com.ritense.authorization.PermissionRepository
import com.ritense.authorization.Role
import com.ritense.authorization.RoleRepository
import com.ritense.authorization.permission.ConditionContainer
import com.ritense.authorization.permission.ContainerPermissionCondition
import com.ritense.authorization.permission.ExpressionPermissionCondition
import com.ritense.authorization.permission.FieldPermissionCondition
import com.ritense.authorization.permission.Permission
import com.ritense.authorization.permission.PermissionConditionOperator.EQUAL_TO
import com.ritense.authorization.permission.PermissionExpressionOperator
import com.ritense.besluit.connector.BesluitProperties
import com.ritense.connector.domain.ConnectorType
import com.ritense.connector.service.ConnectorService
import com.ritense.contactmoment.connector.ContactMomentProperties
import com.ritense.document.domain.event.DocumentDefinitionDeployedEvent
import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.document.domain.impl.searchfield.SearchField
import com.ritense.document.service.DocumentDefinitionService
import com.ritense.document.service.JsonSchemaDocumentActionProvider
import com.ritense.document.service.SearchFieldActionProvider
import com.ritense.haalcentraal.brp.connector.HaalCentraalBrpProperties
import com.ritense.note.domain.Note
import com.ritense.note.service.NoteActionProvider
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
import com.ritense.processdocument.domain.impl.request.DocumentDefinitionProcessRequest
import com.ritense.processdocument.service.DocumentDefinitionProcessLinkService
import com.ritense.valtimo.contract.authentication.AuthoritiesConstants
import com.ritense.valtimo.contract.authentication.AuthoritiesConstants.ADMIN
import com.ritense.valtimo.contract.authentication.AuthoritiesConstants.USER
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
    private val documentDefinitionProcessLinkService: DocumentDefinitionProcessLinkService,
    private val permissionRepository: PermissionRepository,
    private val roleRepository: RoleRepository
) {

    @EventListener(ApplicationReadyEvent::class)
    fun handleApplicationReady() {
        // TODO: support auto-deployment
        createDefaultPermissionsIfNotExists(permissionRepository, roleRepository)
        createConnectors()
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

    fun List<ConnectorType>.findId(connectorName: String): UUID {
        return this.first { it.name == connectorName }
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

    private fun setDocumentDefinitionRole(event: DocumentDefinitionDeployedEvent) {
        documentDefinitionService.putDocumentDefinitionRoles(
            event.documentDefinition().id().name(),
            setOf(AuthoritiesConstants.ADMIN, AuthoritiesConstants.USER)
        )
    }

    private fun createDefaultPermissionsIfNotExists(
        permissionRepository: PermissionRepository,
        roleRepository: RoleRepository
    ) {

        if (!roleRepository.existsById(USER)) {
            roleRepository.save(Role(USER))
        }

        if (!roleRepository.existsById(ADMIN)) {
            roleRepository.save(Role(ADMIN))
        }

        permissionRepository.deleteAll(permissionRepository.findAllByRoleKeyIn(listOf(USER, ADMIN)))

        val documentPermissions: List<Permission> = try {
            listOf(
                // ROLE_USER
                Permission(
                    resourceType = JsonSchemaDocument::class.java,
                    action = JsonSchemaDocumentActionProvider.LIST_VIEW,
                    conditionContainer = ConditionContainer(
                        listOf(
                            ExpressionPermissionCondition(
                                "content.content",
                                "$.height",
                                PermissionExpressionOperator.LESS_THAN, 20000, Int::class.java
                            ),
                            FieldPermissionCondition("documentDefinitionId.name", EQUAL_TO, "leningen")
                        )
                    ),
                    roleKey = USER
                ),
                Permission(
                    resourceType = JsonSchemaDocument::class.java,
                    action = JsonSchemaDocumentActionProvider.LIST_VIEW,
                    conditionContainer = ConditionContainer(
                        listOf(
                            FieldPermissionCondition("assigneeId", EQUAL_TO, "\${currentUserId}")
                        )
                    ),
                    roleKey = USER
                ),
                Permission(
                    resourceType = JsonSchemaDocument::class.java,
                    action = JsonSchemaDocumentActionProvider.VIEW,
                    conditionContainer = ConditionContainer(
                        listOf(
                            ExpressionPermissionCondition(
                                "content.content",
                                "$.height",
                                PermissionExpressionOperator.LESS_THAN, 20000, Int::class.java
                            ),
                            FieldPermissionCondition("documentDefinitionId.name", EQUAL_TO, "leningen")
                        )
                    ),
                    roleKey = USER
                ),
                Permission(
                    resourceType = JsonSchemaDocument::class.java,
                    action = JsonSchemaDocumentActionProvider.VIEW,
                    conditionContainer = ConditionContainer(
                        listOf(
                            FieldPermissionCondition("assigneeId", EQUAL_TO, "\${currentUserId}")
                        )
                    ),
                    roleKey = USER
                ),
                Permission(
                    resourceType = JsonSchemaDocument::class.java,
                    action = JsonSchemaDocumentActionProvider.CLAIM,
                    conditionContainer = ConditionContainer(
                        listOf(
                            FieldPermissionCondition("assigneeId", EQUAL_TO, "\${currentUserId}")
                        )
                    ),
                    roleKey = USER
                ),
                Permission(
                    resourceType = JsonSchemaDocument::class.java,
                    action = JsonSchemaDocumentActionProvider.ASSIGNABLE,
                    conditionContainer = ConditionContainer(emptyList()),
                    roleKey = USER
                ),
                Permission(
                    resourceType = SearchField::class.java,
                    action = SearchFieldActionProvider.LIST_VIEW,
                    conditionContainer = ConditionContainer(
                        listOf(
                            FieldPermissionCondition("id.documentDefinitionName", EQUAL_TO, "leningen")
                        )
                    ),
                    roleKey = ADMIN
                ),
                // ROLE_ADMIN
                Permission(
                    resourceType = JsonSchemaDocument::class.java,
                    action = JsonSchemaDocumentActionProvider.LIST_VIEW,
                    conditionContainer = ConditionContainer(emptyList()),
                    roleKey = ADMIN
                ),
                Permission(
                    resourceType = JsonSchemaDocument::class.java,
                    action = JsonSchemaDocumentActionProvider.VIEW,
                    conditionContainer = ConditionContainer(emptyList()),
                    roleKey = ADMIN
                ),
                Permission(
                    resourceType = JsonSchemaDocument::class.java,
                    action = JsonSchemaDocumentActionProvider.CLAIM,
                    conditionContainer = ConditionContainer(emptyList()),
                    roleKey = ADMIN
                ),
                Permission(
                    resourceType = JsonSchemaDocument::class.java,
                    action = JsonSchemaDocumentActionProvider.ASSIGN,
                    conditionContainer = ConditionContainer(emptyList()),
                    roleKey = ADMIN
                ),
            )
        } catch (e: ClassNotFoundException) {
            listOf()
        }

        val notePermissions: List<Permission> = try {
            listOf(
                // ROLE_USER
                Permission(
                    resourceType = Note::class.java,
                    action = NoteActionProvider.VIEW,
                    conditionContainer = ConditionContainer(
                        listOf(
                            ContainerPermissionCondition(
                                JsonSchemaDocument::class.java,
                                listOf(
                                    FieldPermissionCondition("documentDefinitionId.name", EQUAL_TO, "leningen"),
                                    FieldPermissionCondition("assigneeId", EQUAL_TO, "\${currentUserId}")
                                )
                            )
                        )
                    ),
                    roleKey = USER
                ),
                // ROLE_ADMIN
                Permission(
                    resourceType = Note::class.java,
                    action = NoteActionProvider.VIEW,
                    conditionContainer = ConditionContainer(listOf()),
                    roleKey = ADMIN
                )
            )

        } catch (e: ClassNotFoundException) {
            listOf()
        }

        permissionRepository.saveAll(documentPermissions + notePermissions)
    }

    companion object {
        val logger = KotlinLogging.logger {}
        const val OPENNOTIFICATIES_CONNECTOR_NAME = "OpenNotificaties"
        const val TAAK_OBJECTAPI_CONNECTOR_NAME = "TaakObjects"
        const val PRODUCTAANVRAAG_OBJECTAPI_CONNECTOR_NAME = "ProductAanvraagObjects"
    }
}
