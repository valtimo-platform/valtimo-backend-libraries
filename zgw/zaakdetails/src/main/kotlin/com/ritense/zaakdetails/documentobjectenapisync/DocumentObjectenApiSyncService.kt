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

package com.ritense.zaakdetails.documentobjectenapisync

import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.document.domain.Document
import com.ritense.document.domain.event.DocumentCreatedEvent
import com.ritense.document.domain.event.DocumentModifiedEvent
import com.ritense.document.service.DocumentService
import com.ritense.objectenapi.ObjectenApiPlugin
import com.ritense.objectenapi.client.Comparator.EQUAL_TO
import com.ritense.objectenapi.client.ObjectRecord
import com.ritense.objectenapi.client.ObjectRequest
import com.ritense.objectenapi.client.ObjectSearchParameter
import com.ritense.objectenapi.client.ObjectWrapper
import com.ritense.objectenapi.management.ObjectManagementInfo
import com.ritense.objectenapi.management.ObjectManagementInfoProvider
import com.ritense.objecttypenapi.ObjecttypenApiPlugin
import com.ritense.plugin.service.PluginService
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.zaakdetails.domain.ZaakdetailsObject
import com.ritense.zaakdetails.service.ZaakdetailsObjectService
import com.ritense.zakenapi.ZaakUrlProvider
import com.ritense.zakenapi.ZakenApiPlugin
import com.ritense.zakenapi.link.ZaakInstanceLinkNotFoundException
import mu.KotlinLogging
import org.springframework.context.event.EventListener
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.net.URI
import java.time.LocalDate

@Transactional
@Service
@SkipComponentScan
class DocumentObjectenApiSyncService(
    private val objectObjectManagementInfoProvider: ObjectManagementInfoProvider,
    private val documentService: DocumentService,
    private val pluginService: PluginService,
    private val zaakUrlProvider: ZaakUrlProvider,
    private val zaakdetailsObjectService: ZaakdetailsObjectService,
    private val documentObjectenApiSyncManagementService: DocumentObjectenApiSyncManagementService,
    private val linkZaakdetailsToZaakEnabled: Boolean
) {
    @EventListener(DocumentCreatedEvent::class)
    fun handleDocumentCreatedEvent(event: DocumentCreatedEvent) {
        logger.info { "handle documentCreatedEvent documentId=${event.documentId()} definitionId=${event.definitionId()}" }
        sync(documentService.get(event.documentId().id.toString()))
    }

    @EventListener(DocumentModifiedEvent::class)
    fun handleDocumentModifiedEvent(event: DocumentModifiedEvent) {
        logger.info { "handle documentModifiedEvent documentId=${event.documentId()}" }
        sync(documentService.get(event.documentId().id.toString()))
    }

    @Deprecated(
        "Since 12.6.0",
        ReplaceWith("com.ritense.zaakdetails.documentobjectenapisync.DocumentObjectenApiSyncManagementServic.getSyncConfiguration")
    )
    fun getSyncConfiguration(
        documentDefinitionName: String,
        documentDefinitionVersion: Long
    ): DocumentObjectenApiSync? {
        return documentObjectenApiSyncManagementService.getSyncConfiguration(
            documentDefinitionName,
            documentDefinitionVersion
        )
    }

    @Deprecated(
        "Since 12.6.0",
        ReplaceWith("com.ritense.zaakdetails.documentobjectenapisync.DocumentObjectenApiSyncManagementService.saveSyncConfiguration")
    )
    fun saveSyncConfiguration(sync: DocumentObjectenApiSync) {
        documentObjectenApiSyncManagementService.saveSyncConfiguration(sync)
    }

    @Deprecated(
        "Since 12.6.0",
        ReplaceWith("com.ritense.zaakdetails.documentobjectenapisync.DocumentObjectenApiSyncManagementService.deleteSyncConfigurationByDocumentDefinition")
    )
    fun deleteSyncConfigurationByDocumentDefinition(documentDefinitionName: String, documentDefinitionVersion: Long) {
        documentObjectenApiSyncManagementService.deleteSyncConfigurationByDocumentDefinition(
            documentDefinitionName,
            documentDefinitionVersion
        )
    }

    private fun sync(document: Document) {
        val syncConfiguration = documentObjectenApiSyncManagementService.getSyncConfiguration(
            document.definitionId().name(),
            document.definitionId().version()
        )
        if (syncConfiguration?.enabled == true) {
            logger.debug { "Sync configuration found for document ${document.id()}" }
            val objectManagementConfiguration =
                objectObjectManagementInfoProvider.getObjectManagementInfo(syncConfiguration.objectManagementConfigurationId)
            val objectenApiPlugin =
                pluginService.createInstance<ObjectenApiPlugin>(objectManagementConfiguration.objectenApiPluginConfigurationId)
            val objecttypenApiPlugin =
                pluginService.createInstance<ObjecttypenApiPlugin>(objectManagementConfiguration.objecttypenApiPluginConfigurationId)

            val objectRequest = getObjectRequest(document, objecttypenApiPlugin, objectManagementConfiguration)

            val zaakdetailsObject: ZaakdetailsObject
            val zaakdetailsObjectOptional = zaakdetailsObjectService.findByDocumentId(document.id().id)

            val checkExistingZaakObjectBeforeCreating: Boolean

            if (zaakdetailsObjectOptional.isPresent) { //Zaakdetails object exists and reference has been stored: update
                logger.debug { "Zaakdetails object already exists: update." }
                zaakdetailsObject = zaakdetailsObjectOptional.get()

                objectenApiPlugin.objectUpdate(zaakdetailsObject.objectURI, objectRequest)

                checkExistingZaakObjectBeforeCreating = false
            } else {
                logger.debug { "Reference to Zaakdetails object does not exist yet." }
                val existingObjectWrapper = getObjectWithCaseId(
                    document,
                    objectenApiPlugin,
                    objectManagementConfiguration,
                    objecttypenApiPlugin
                )

                if (existingObjectWrapper != null) { //Zaakdetails object exists, but reference has not been stored: update and store reference
                    objectenApiPlugin.objectUpdate(existingObjectWrapper.url, objectRequest)

                    zaakdetailsObject = ZaakdetailsObject(
                        documentId = document.id().id,
                        objectURI = existingObjectWrapper.url
                    )

                    checkExistingZaakObjectBeforeCreating = true
                } else {
                    logger.debug { "Zaakdetails object does not exist yet: create." }
                    val newObjectWrapper = objectenApiPlugin.createObject(objectRequest)
                    zaakdetailsObject = ZaakdetailsObject(
                        documentId = document.id().id,
                        objectURI = newObjectWrapper.url
                    )

                    //New zaakdetails object, the zaakobject can't exist yet
                    checkExistingZaakObjectBeforeCreating = false
                }

                zaakdetailsObjectService.save(zaakdetailsObject)
            }

            if (!zaakdetailsObject.linkedToZaak && linkZaakdetailsToZaakEnabled) {
                createZaakObjectIfNotExisting(zaakdetailsObject, checkExistingZaakObjectBeforeCreating)
            }
        }
    }

    private fun getObjectRequest(
        document: Document,
        objecttypenApiPlugin: ObjecttypenApiPlugin,
        objectManagementConfiguration: ObjectManagementInfo
    ): ObjectRequest {
        val content = document.content().asJson() as ObjectNode
        content.put("caseId", document.id().id.toString())

        return ObjectRequest(
            type = objecttypenApiPlugin.getObjectTypeUrlById(objectManagementConfiguration.objecttypeId),
            record = ObjectRecord(
                typeVersion = objectManagementConfiguration.objecttypeVersion,
                data = content,
                startAt = LocalDate.now()
            )
        )
    }

    private fun getObjectWithCaseId(
        document: Document,
        objectenApiPlugin: ObjectenApiPlugin,
        objectManagementConfiguration: ObjectManagementInfo,
        objecttypenApiPlugin: ObjecttypenApiPlugin
    ): ObjectWrapper? {

        val searchString = ObjectSearchParameter.toQueryParameter(
            ObjectSearchParameter("caseId", EQUAL_TO, document.id().toString())
        )

        val results = objectenApiPlugin.getObjectsByObjectTypeIdWithSearchParams(
            objecttypesApiUrl = objecttypenApiPlugin.url,
            objecttypeId = objectManagementConfiguration.objecttypeId,
            searchString = searchString,
            pageable = PageRequest.of(0, 2)
        ).results
        require(results.size <= 1) { "Failed to Sync document to Objecten API: Found multiple sync targets" }
        return results.firstOrNull()
    }

    private fun createZaakObjectIfNotExisting(
        zaakdetailsObject: ZaakdetailsObject,
        checkExistingZaakObjectBeforeCreating: Boolean
    ) {
        logger.debug { "Create Zaakobject. Check if existing: ${checkExistingZaakObjectBeforeCreating}" }
        try {
            val zaakUri = zaakUrlProvider.getZaakUrl(zaakdetailsObject.documentId)

            val zakenApiPlugin = pluginService.createInstance(
                ZakenApiPlugin::class.java,
                ZakenApiPlugin.findConfigurationByUrl(zaakUri)
            )

            if (zakenApiPlugin == null) {
                logger.debug { "Zaken API plugin has not been configured: can't link the Zaakdetails object to the Zaak" }
                return
            }

            if (checkExistingZaakObjectBeforeCreating) {
                val zaakobjectExists = zakenApiPlugin.getZaakObject(zaakUri, zaakdetailsObject.objectURI) != null
                if (!zaakobjectExists) {
                    logger.debug { "Zaakdetails object has not been linked to the Zaak yet" }
                    createZaakObject(zaakUri, zakenApiPlugin, zaakdetailsObject)
                }
            } else {
                createZaakObject(zaakUri, zakenApiPlugin, zaakdetailsObject)
            }

            zaakdetailsObject.linkedToZaak = true
            zaakdetailsObjectService.save(zaakdetailsObject)
        } catch (e: ZaakInstanceLinkNotFoundException) {
            logger.debug { "Zaak does not exist yet: can't link the Zaakdetails object to the Zaak" }
            return
        }
    }

    private fun createZaakObject(
        zaakUri: URI,
        zakenApiPlugin: ZakenApiPlugin,
        zaakdetailsObject: ZaakdetailsObject
    ) {
        zakenApiPlugin.createZaakObject(
            zaakUrl = zaakUri,
            objectUrl = zaakdetailsObject.objectURI,
            objectTypeOverige = "zaakdetails",
            documentId = zaakdetailsObject.documentId
        )
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }

}
