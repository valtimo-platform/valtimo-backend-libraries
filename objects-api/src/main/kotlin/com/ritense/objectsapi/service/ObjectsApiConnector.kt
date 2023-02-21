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

package com.ritense.objectsapi.service

import com.fasterxml.jackson.core.JsonPointer
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.ritense.connector.domain.Connector
import com.ritense.connector.domain.ConnectorProperties
import com.ritense.connector.domain.meta.ConnectorType
import com.ritense.document.domain.impl.JsonSchemaDocumentId
import com.ritense.document.service.DocumentService
import com.ritense.objectsapi.domain.Object
import com.ritense.objectsapi.domain.Record
import com.ritense.objectsapi.domain.request.CreateObjectRequest
import com.ritense.objectsapi.domain.request.ModifyObjectRequest
import com.ritense.valtimo.contract.json.Mapper
import org.springframework.core.ParameterizedTypeReference
import java.net.URI
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

@ConnectorType(name = "ObjectsApi")
class ObjectsApiConnector(
    objectsApiProperties: ObjectsApiProperties,
    private var documentService: DocumentService
) : Connector, ObjectsApiService(objectsApiProperties) {

    var payload: MutableMap<String, JsonNode> = mutableMapOf()
    lateinit var rawPayload: JsonNode
    var typeVersion: String = ""

    /**
     * Overrides typeVersion in objectTypeConfig.
     *
     * @param version the <code>version</code> to use when creating new object request
     */
    fun typeVersion(version: String): ObjectsApiConnector {
        this.typeVersion = version
        return this
    }

    fun payload(content: JsonNode): ObjectsApiConnector {
        this.rawPayload = content
        return this
    }

    fun put(documentId: String, key: String, pathToValue: String): ObjectsApiConnector {
        val document = documentService.findBy(JsonSchemaDocumentId.existingId(UUID.fromString(documentId))).orElseThrow()
        this.payload[key] = document.content().getValueBy(JsonPointer.valueOf(pathToValue)).orElseThrow()
        return this
    }

    fun executeCreateObjectRequest(): Object {
        val typeVersion = this.typeVersion.ifBlank { objectsApiProperties.objectType.typeVersion }
        val payload = this.payload.ifEmpty {
            Mapper.INSTANCE.get().convertValue(rawPayload, object : TypeReference<Map<String, Any>>() {})
        }
        return createObject(
            CreateObjectRequest(
                URI.create(objectsApiProperties.objectType.url),
                Record(
                    typeVersion = typeVersion,
                    startAt = LocalDate.now().format(DateTimeFormatter.ofPattern(datePattern)),
                    data = payload
                )
            )
        )
    }

    fun executeModifyObjectRequest(uuid: UUID): Object {
        val typeVersion = this.typeVersion.ifBlank { objectsApiProperties.objectType.typeVersion }
        val payload = this.payload.ifEmpty {
            Mapper.INSTANCE.get().convertValue(rawPayload, object : TypeReference<Map<String, Any>>() {})
        }
        return modifyObject(
            ModifyObjectRequest(
                uuid = uuid,
                type = URI.create(objectsApiProperties.objectType.url),
                Record(
                    typeVersion = typeVersion,
                    startAt = LocalDate.now().format(DateTimeFormatter.ofPattern(datePattern)),
                    data = payload
                )
            )
        )
    }

    override fun getProperties(): ObjectsApiProperties {
        return objectsApiProperties
    }

    override fun setProperties(connectorProperties: ConnectorProperties) {
        objectsApiProperties = connectorProperties as ObjectsApiProperties
    }

    companion object {
        const val rootUrlApiVersion = "/api/v2"
        const val datePattern = "yyyy-MM-dd"
        inline fun <reified T> typeReference() = object : ParameterizedTypeReference<T>() {}
    }
}