package com.ritense.gzac.listener

import com.ritense.connector.service.ConnectorService
import com.ritense.objectsapi.service.ObjectSyncService
import com.ritense.objectsapi.service.ObjectTypeConfig
import com.ritense.objectsapi.service.ObjectsApiProperties
import com.ritense.objectsapi.service.ServerAuthSpecification
import com.ritense.objectsapi.web.rest.request.CreateObjectSyncConfigRequest
import com.ritense.openzaak.domain.configuration.Rsin
import com.ritense.openzaak.domain.connector.OpenZaakConfig
import com.ritense.openzaak.domain.connector.OpenZaakProperties
import mu.KotlinLogging
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ApplicationReadyEventListener(
    private val connectorService: ConnectorService,
    private val objectSyncService: ObjectSyncService
) {

    @EventListener(ApplicationReadyEvent::class)
    fun handle() {
        connectorService.getConnectorTypes().forEach {
            try {
                if (it.name == "OpenZaak") {
                    connectorService.createConnectorInstance(
                        typeId = it.id.id,
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
                if (it.name == "ContactMoment") {
                    connectorService.createConnectorInstance(
                        typeId = it.id.id,
                        name = "ContactMomentInstance",
                        connectorProperties = OpenZaakProperties(
                            OpenZaakConfig(
                                "http://localhost:8006",
                                "valtimo_client",
                                "e09b8bc5-5831-4618-ab28-41411304309d",
                                Rsin("051845623")
                            )
                        )
                    )
                }
                if (it.name == "ObjectsApi") {
                    val result = connectorService.createConnectorInstance(
                        typeId = it.id.id,
                        name = "ObjectsApiInstance",
                        connectorProperties = ObjectsApiProperties(
                            objectsApi = ServerAuthSpecification(
                                "http://localhost:8000",
                                "cd63e158f3aca276ef284e3033d020a22899c728"
                            ),
                            objectsTypeApi = ServerAuthSpecification(
                                "http://localhost:8001",
                                "cd63e158f3aca276ef284e3033d020a22899c728"
                            ),
                            objectType = ObjectTypeConfig(
                                "straatverlichting",
                                "Straatverlichting",
                                "http://172.17.0.1:8001/api/v1/objecttypes/3a82fb7f-fc9b-4104-9804-993f639d6d0d",
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
            } catch (ex: Exception) {
                logger.error { ex }
            }
        }
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}