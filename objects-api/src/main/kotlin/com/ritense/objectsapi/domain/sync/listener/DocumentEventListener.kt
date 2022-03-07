/*
 * Copyright 2015-2020 Ritense BV, the Netherlands.
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

package com.ritense.objectsapi.domain.sync.listener

import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.connector.service.ConnectorFluentBuilder
import com.ritense.connector.service.ConnectorService
import com.ritense.document.domain.impl.event.JsonSchemaDocumentCreatedEvent
import com.ritense.document.domain.impl.event.JsonSchemaDocumentModifiedEvent
import com.ritense.document.service.DocumentService
import com.ritense.objectsapi.domain.request.Comparator
import com.ritense.objectsapi.domain.request.ObjectSearchParameter
import com.ritense.objectsapi.service.ObjectSyncService
import com.ritense.objectsapi.service.ObjectsApiConnector
import com.ritense.objectsapi.service.ObjectsApiProperties
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import java.net.URI

open class DocumentEventListener(
    val objectSyncService: ObjectSyncService,
    val connectorService: ConnectorService,
    val connectorFluentBuilder: ConnectorFluentBuilder,
    val documentService: DocumentService
) {

    @TransactionalEventListener(
        phase = TransactionPhase.AFTER_COMMIT,
        classes = [JsonSchemaDocumentCreatedEvent::class]
    )
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    open fun handleDocumentCreatedEvent(event: JsonSchemaDocumentCreatedEvent) {
        val objectSyncConfig = objectSyncService.getObjectSyncConfig(event.definitionId().name())
        objectSyncConfig.content.forEach {
            when {
                it.enabled -> {
                    val connectorInstance = connectorService.getConnectorInstanceById(it.connectorInstanceId)

                    val objectsApiConnector = connectorFluentBuilder
                        .builder()
                        .withConnector(connectorInstance.name) as ObjectsApiConnector

                    val document = documentService.findBy(event.documentId()).orElseThrow()
                    val content = document.content().asJson() as ObjectNode
                    content.put("caseId", event.documentId().id.toString())
                    objectsApiConnector.payload(content)
                    objectsApiConnector.executeCreateObjectRequest()
                }
            }
        }
    }

    @TransactionalEventListener(
        phase = TransactionPhase.AFTER_COMMIT,
        classes = [JsonSchemaDocumentModifiedEvent::class]
    )
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    open fun handleDocumentModifiedEvent(event: JsonSchemaDocumentModifiedEvent) {
        val document = documentService.findBy(event.documentId()).orElseThrow()
        val objectSyncConfig = objectSyncService.getObjectSyncConfig(document.definitionId().name())
        objectSyncConfig.content.forEach {
            when {
                it.enabled -> {
                    val connectorInstance = connectorService.getConnectorInstanceById(it.connectorInstanceId)

                    val objectsApiConnector = connectorFluentBuilder
                        .builder()
                        .withConnector(connectorInstance.name) as ObjectsApiConnector

                    val objectsApiProperties = objectsApiConnector.getProperties()

                    //Find object to update
                    val objects = objectsApiConnector.getObjects(
                        type = URI.create(objectsApiProperties.objectType.url),
                        searchParams = listOf(
                            ObjectSearchParameter("caseId", Comparator.EQUAL, event.documentId().id.toString())
                        )
                    )

                    val content = document.content().asJson() as ObjectNode
                    content.put("caseId", event.documentId().id.toString())
                    objectsApiConnector.payload(content)

                    if (objects.isEmpty()) {
                        objectsApiConnector.executeCreateObjectRequest()
                    } else {
                        objectsApiConnector.executeModifyObjectRequest(objects.stream().findFirst().get().uuid)
                    }
                }
            }
        }
    }
}