/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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
import com.ritense.objectenapi.management.ObjectManagementInfoProvider
import com.ritense.objecttypenapi.ObjecttypenApiPlugin
import com.ritense.objecttypenapi.client.Objecttype
import com.ritense.openzaak.service.ZaakInstanceLinkService
import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.plugin.service.PluginService
import com.ritense.zakenapi.ZakenApiPlugin
import mu.KotlinLogging
import java.net.URI
import java.time.LocalDate
import java.util.UUID

class ZaakObjectService(
    val zaakInstanceLinkService: ZaakInstanceLinkService,
    val pluginService : PluginService,
    val formDefinitionService : FormDefinitionService,
    val objectManagementInfoProvider: ObjectManagementInfoProvider
) {
    fun getZaakObjectTypes(documentId: UUID): List<Objecttype> {
        val zaakUrl = zaakInstanceLinkService.getByDocumentId(documentId).zaakInstanceUrl
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

    private fun getObjectByObjectUrl(objectUrl: URI) : ObjectWrapper? {
        val objectenApiPlugin = pluginService
            .createInstance(ObjectenApiPlugin::class.java) { properties: JsonNode ->
                objectUrl.toString().startsWith(properties.get("url").textValue())
            }?: return null
        return objectenApiPlugin.getObject(objectUrl)
    }

    private fun getObjectTypeByUrl(objectTypeUrl: URI): Objecttype? {
        val objectTypePluginInstance = pluginService
            .createInstance(ObjecttypenApiPlugin::class.java) { properties: JsonNode ->
                objectTypeUrl.toString().startsWith(properties.get("url").textValue())
            }?: return null

        return objectTypePluginInstance.getObjecttype(objectTypeUrl)
    }

    fun getZaakObjectenOfType(documentId: UUID, typeUrl: URI): List<ObjectWrapper> {
        val zaakUrl = zaakInstanceLinkService.getByDocumentId(documentId).zaakInstanceUrl
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
        val zaakUrl = zaakInstanceLinkService.getByDocumentId(documentId).zaakInstanceUrl
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

    fun getZaakObjectForm(objectUrl: URI): FormDefinition? {
        logger.debug { "Getting object for url $objectUrl" }
        val theObject = getObjectByObjectUrl(objectUrl)
        return theObject?.let {
            logger.trace { "Getting objecttype for object $theObject" }
            getObjectTypeByUrl(it.type)
        }?.let {
            val formName = "${it.name}$FORM_SUFFIX"
            logger.trace { "Getting form for objecttype $it with formName $formName" }
            formDefinitionService.getFormDefinitionByNameIgnoringCase(formName)
        }
        ?.orElse(null)
        ?.preFill(theObject.record.data)
    }

    private fun findZakenApiPlugin(zaakUrl: URI): ZakenApiPlugin {
        val zakenApiPluginInstance = pluginService
            .createInstance(ZakenApiPlugin::class.java) { properties: JsonNode ->
                zaakUrl.toString().startsWith(properties.get("url").textValue())
            }

        requireNotNull(zakenApiPluginInstance) { "No plugin configuration was found for zaak with URL $zaakUrl" }

        return zakenApiPluginInstance
    }

    fun createObject(objectManagementId: UUID, data: JsonNode) : URI {
        val objectManagementInfo = objectManagementInfoProvider.getObjectManagementInfo(objectManagementId)

        val objecttypeApiPlugin = pluginService.createInstance(PluginConfigurationId(objectManagementInfo.objecttypenApiPluginConfigurationId)) as ObjecttypenApiPlugin
        val objectTypeUrl = objecttypeApiPlugin.getObjectTypeUrlById(objectManagementInfo.objecttypeId)

        val createObjectRequest = ObjectRequest(
            objectTypeUrl,
            ObjectRecord(
                typeVersion = objectManagementInfo.objecttypeVersion,
                data = data,
                startAt = LocalDate.now()
            )
        )

        val objectenApiPlugin = pluginService.createInstance(PluginConfigurationId(objectManagementInfo.objectenApiPluginConfigurationId)) as ObjectenApiPlugin
        return objectenApiPlugin.createObject(createObjectRequest).url
    }

    companion object {
        const val FORM_SUFFIX = ".editform"
        val logger = KotlinLogging.logger {}
    }
}