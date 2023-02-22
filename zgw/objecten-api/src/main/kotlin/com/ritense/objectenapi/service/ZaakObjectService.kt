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

package com.ritense.objectenapi.service

import com.fasterxml.jackson.databind.JsonNode
import com.ritense.form.domain.FormDefinition
import com.ritense.form.service.FormDefinitionService
import com.ritense.objectenapi.ObjectenApiPlugin
import com.ritense.objectenapi.client.ObjectRecord
import com.ritense.objectenapi.client.ObjectRequest
import com.ritense.objectenapi.client.ObjectWrapper
import com.ritense.objectenapi.management.ObjectManagementInfo
import com.ritense.objectenapi.management.ObjectManagementInfoProvider
import com.ritense.objectenapi.web.rest.result.FormType
import com.ritense.objecttypenapi.ObjecttypenApiPlugin
import com.ritense.objecttypenapi.client.Objecttype
import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.plugin.service.PluginService
import com.ritense.zakenapi.ZaakUrlProvider
import com.ritense.zakenapi.ZakenApiPlugin
import java.net.URI
import java.time.LocalDate
import java.util.UUID
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.web.util.UriComponentsBuilder

class ZaakObjectService(
    val zaakUrlProvider: ZaakUrlProvider,
    val pluginService: PluginService,
    val formDefinitionService: FormDefinitionService,
    val objectManagementInfoProvider: ObjectManagementInfoProvider
) {
    fun getZaakObjectTypes(documentId: UUID): List<Objecttype> {
        val zaakUrl = URI(zaakUrlProvider.getZaak(documentId))
        val zakenApiPluginInstance = findZakenApiPlugin(zaakUrl)

        return zakenApiPluginInstance.getZaakObjecten(zaakUrl)
            .mapNotNull {
                getObjectByObjectUrl(it.objectUrl)
            }.map { it.type }
            .distinct()
            .mapNotNull {
                getObjectTypeByUrl(it)
            }
    }

    private fun getObjectByObjectUrl(objectUrl: URI): ObjectWrapper? {
        val objectenApiPlugin = pluginService
            .createInstance(ObjectenApiPlugin::class.java) { properties: JsonNode ->
                objectUrl.toString().startsWith(properties.get("url").textValue())
            } ?: return null
        return objectenApiPlugin.getObject(objectUrl)
    }

    private fun getObjectTypeByUrl(objectTypeUrl: URI): Objecttype? {
        val objectTypePluginInstance = pluginService
            .createInstance(ObjecttypenApiPlugin::class.java) { properties: JsonNode ->
                objectTypeUrl.toString().startsWith(properties.get("url").textValue())
            } ?: return null

        return objectTypePluginInstance.getObjecttype(objectTypeUrl)
    }

    fun getZaakObjectenOfType(documentId: UUID, typeUrl: URI): List<ObjectWrapper> {
        val zaakUrl = URI(zaakUrlProvider.getZaak(documentId))
        val zakenApiPluginInstance = findZakenApiPlugin(zaakUrl)

        return zakenApiPluginInstance.getZaakObjecten(zaakUrl)
            .mapNotNull {
                getObjectByObjectUrl(it.objectUrl)
            }.filter {
                it.type == typeUrl
            }
    }

    fun getZaakObjectOfTypeByName(documentId: UUID, objecttypeName: String): ObjectWrapper {
        logger.debug { "Getting zaakobject for documentId $documentId and objecttypeName '$objecttypeName'" }
        val zaakUrl = URI(zaakUrlProvider.getZaak(documentId))
        val zakenApiPluginInstance = findZakenApiPlugin(zaakUrl)

        val listOfObjecttypeWithCorrectName = zakenApiPluginInstance.getZaakObjecten(zaakUrl)
            .mapNotNull {
                getObjectByObjectUrl(it.objectUrl)
            }.groupBy {
                it.type
            }.filter {
                getObjectTypeByUrl(it.key)?.name == objecttypeName
            }.map {
                it.value
            }

        if (listOfObjecttypeWithCorrectName.isEmpty()) {
            throw IllegalStateException("No object was found of type '$objecttypeName' for document $documentId")
        } else if (listOfObjecttypeWithCorrectName.size > 1) {
            throw IllegalStateException("More than one objecttype with name '$objecttypeName' was found for document $documentId")
        } else {
            val objectsOfType = listOfObjecttypeWithCorrectName[0]
            if (objectsOfType.size > 1) {
                throw IllegalStateException("More than one object of type '$objecttypeName' was found for document $documentId")
            } else {
                return objectsOfType[0]
            }
        }
    }

    fun getZaakObjectForm(
        objectUrl: URI? = null,
        objectManagementId: UUID? = null,
        objectId: UUID? = null,
        formType: FormType? = null
    ): FormDefinition? {
        val theObject = if (objectUrl == null) {
            if (objectManagementId == null || formType == null) {
                throw IllegalStateException("If the objectUrl is null you need to provide all of the following values: objectManagementId and formType")
            } else if (objectId == null) {
                null
            } else {
                getObjectByManagementIdAndObjectId(objectManagementId, objectId)
            }
        } else {
            logger.debug { "Getting object for url $objectUrl" }
            getObjectByObjectUrl(objectUrl)
        }

        val formDefinition = theObject?.let {
            logger.trace { "Getting objecttype for object $theObject" }
            getObjectTypeByUrl(it.type)
        }?.let {
            val formName = if (formType == null) {
                "${it.name}$FORM_SUFFIX"
            } else if (formType == FormType.EDITFORM) {
                objectManagementInfoProvider.getObjectManagementInfo(objectManagementId!!).formDefinitionEdit
                    ?: throw IllegalStateException("The form definition edit value is not configured")
            } else {
                objectManagementInfoProvider.getObjectManagementInfo(objectManagementId!!).formDefinitionView
                    ?: throw IllegalStateException("The form definition summary value is not configured")
            }
            logger.trace { "Getting form for objecttype $it with formName $formName" }
            formDefinitionService.getFormDefinitionByNameIgnoringCase(formName)
        }
            ?.orElse(null)
            ?.preFill(theObject.record.data)

        return if (theObject == null && objectManagementId != null) {
            formDefinitionService.getFormDefinitionByNameIgnoringCase(
                objectManagementInfoProvider.getObjectManagementInfo(objectManagementId).formDefinitionEdit
            )?.orElse(null)
        } else {
            formDefinition
        }
    }

    private fun getObjectByManagementIdAndObjectId(objectManagementId: UUID, objectId: UUID): ObjectWrapper? {
        val objectManagement =
            objectManagementInfoProvider.getObjectManagementInfo(objectManagementId)
        val objectsApiPlugin =
            pluginService.createInstance(PluginConfigurationId(objectManagement.objectenApiPluginConfigurationId)) as ObjectenApiPlugin
        val objectUrl = "${objectsApiPlugin.url}objects/$objectId"
        logger.debug { "Getting object for url $objectUrl" }
        return objectsApiPlugin.getObject(URI.create(objectUrl))
    }

    private fun findZakenApiPlugin(zaakUrl: URI): ZakenApiPlugin {
        val zakenApiPluginInstance = pluginService
            .createInstance(ZakenApiPlugin::class.java) { properties: JsonNode ->
                zaakUrl.toString().startsWith(properties.get("url").textValue())
            }

        requireNotNull(zakenApiPluginInstance) { "No plugin configuration was found for zaak with URL $zaakUrl" }

        return zakenApiPluginInstance
    }

    private fun getObjectRequestAndInfo(
        objectManagementId: UUID,
        data: JsonNode
    ): Pair<ObjectRequest, ObjectManagementInfo> {
        val objectManagementInfo = objectManagementInfoProvider.getObjectManagementInfo(objectManagementId)

        val objecttypeApiPlugin =
            pluginService.createInstance(PluginConfigurationId(objectManagementInfo.objecttypenApiPluginConfigurationId)) as ObjecttypenApiPlugin
        val objectTypeUrl = objecttypeApiPlugin.getObjectTypeUrlById(objectManagementInfo.objecttypeId)

        val objectRequest = ObjectRequest(
            objectTypeUrl,
            ObjectRecord(
                typeVersion = objectManagementInfo.objecttypeVersion,
                data = data,
                startAt = LocalDate.now()
            )
        )

        return Pair(objectRequest, objectManagementInfo)
    }


    fun createObject(objectManagementId: UUID, data: JsonNode): URI {
        val (createObjectRequest, objectManagementInfo) = getObjectRequestAndInfo(objectManagementId, data)

        val objectenApiPlugin =
            pluginService.createInstance(PluginConfigurationId(objectManagementInfo.objectenApiPluginConfigurationId)) as ObjectenApiPlugin
        return objectenApiPlugin.createObject(createObjectRequest).url
    }

    fun updateObject(objectManagementId: UUID, objectUrl: URI, data: JsonNode): URI {
        val (updateObjectRequest, objectManagementInfo) = getObjectRequestAndInfo(objectManagementId, data)

        val objectenApiPlugin = pluginService.createInstance(
            PluginConfigurationId(
                objectManagementInfo.objectenApiPluginConfigurationId
            )
        ) as ObjectenApiPlugin

        return objectenApiPlugin.objectUpdate(
            objectUrl,
            updateObjectRequest
        ).url
    }

    fun deleteObject(objectManagementId: UUID, objectId: UUID? = null, objectUrl: URI? = null): HttpStatus {
        val objectManagementInfo = objectManagementInfoProvider.getObjectManagementInfo(objectManagementId)

        val objectenApiPlugin = pluginService.createInstance(
            PluginConfigurationId(
                objectManagementInfo.objectenApiPluginConfigurationId
            )
        ) as ObjectenApiPlugin

        if (objectId == null && objectUrl == null) {
            throw IllegalStateException("The objectUrl and objectId can not both be null.")
        }

        val objectUri = objectUrl ?: URI.create(
                    UriComponentsBuilder.newInstance()
                        .uri(objectenApiPlugin.url)
                        .pathSegment("objects")
                        .pathSegment(objectId.toString())
                        .toUriString()
                )

        return objectenApiPlugin.deleteObject(objectUri)
    }

    fun patchObjectFromManagementId(objectManagementId: UUID, objectId: UUID, jsonNode: JsonNode): URI {
        val objectManagement = objectManagementInfoProvider.getObjectManagementInfo(objectManagementId)

        val objectenApiPlugin = pluginService.createInstance(
            PluginConfigurationId.existingId(objectManagement.objectenApiPluginConfigurationId)
        ) as ObjectenApiPlugin

        val objectTypenApiPlugin = pluginService.createInstance(
            PluginConfigurationId.existingId(objectManagement.objectenApiPluginConfigurationId)
        ) as ObjecttypenApiPlugin

        val objectRequest = ObjectRequest(
            objectTypenApiPlugin.url,
            ObjectRecord(
                typeVersion = objectManagement.objecttypeVersion,
                data = jsonNode,
                startAt = LocalDate.now()
            )
        )

        val objectUrl = URI.create(
            UriComponentsBuilder.newInstance()
                .uri(objectenApiPlugin.url)
                .pathSegment("objects")
                .pathSegment(objectId.toString())
                .toUriString()
        )

        return objectenApiPlugin.objectPatch(objectUrl, objectRequest).url
    }

    companion object {
        const val FORM_SUFFIX = ".editform"
        val logger = KotlinLogging.logger {}
    }
}
