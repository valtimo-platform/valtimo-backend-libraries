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

package com.ritense.objectsapi.service

import com.fasterxml.jackson.core.JsonPointer
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.ritense.connector.domain.Connector
import com.ritense.connector.domain.ConnectorProperties
import com.ritense.connector.domain.meta.ConnectorType
import com.ritense.document.domain.impl.JsonSchemaDocumentId
import com.ritense.document.service.DocumentService
import com.ritense.objectsapi.domain.GenericObject
import com.ritense.objectsapi.domain.Object
import com.ritense.objectsapi.domain.ObjectTypes
import com.ritense.objectsapi.domain.Record
import com.ritense.objectsapi.domain.request.CreateObjectRequest
import com.ritense.objectsapi.domain.request.ModifyObjectRequest
import com.ritense.objectsapi.domain.request.ObjectSearchParameter
import com.ritense.valtimo.contract.json.Mapper
import java.net.URI
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import org.springframework.core.ParameterizedTypeReference

@ConnectorType(name = "ObjectsApi")
class ObjectsApiConnector(
    private var objectsApiProperties: ObjectsApiProperties,
    private var documentService: DocumentService
) : Connector {

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

    // ObjectsType methods:

    /**
     * Retrieve a list of OBJECTTYPEs.
     */
    fun objectTypes(): Collection<ObjectTypes> {
        return RequestBuilder
            .builder()
            .baseUrl(objectsApiProperties.objectsTypeApi.url)
            .token(objectsApiProperties.objectsTypeApi.token)
            .path("$rootUrlApiVersion/objecttypes")
            .get()
            .executeForCollection(ObjectTypes::class.java)
    }

    // Objects methods

    /**
     * Create an OBJECT and its initial RECORD.
     *
     * @param request the <code>CreateObjectRequest</code> to use when creating new requests
     */
    fun createObject(request: CreateObjectRequest): Object {
        return RequestBuilder
            .builder()
            .baseUrl(objectsApiProperties.objectsApi.url)
            .token(objectsApiProperties.objectsApi.token)
            .path("$rootUrlApiVersion/objects")
            .post()
            .body(request)
            .execute(Object::class.java)
    }

    /**
     * Update the OBJECT by creating a new RECORD with the updates values.
     *
     * @param request the <code>ModifyObjectRequest</code> to use when modifying an Objects record
     */
    fun modifyObject(request: ModifyObjectRequest): Object {
        return RequestBuilder
            .builder()
            .baseUrl(objectsApiProperties.objectsApi.url)
            .token(objectsApiProperties.objectsApi.token)
            .path("$rootUrlApiVersion/objects/${request.uuid}")
            .put()
            .body(request)
            .execute(Object::class.java)
    }

    /**
     * Retrieve a list of OBJECTs and their actual RECORD.
     * The actual record is defined as if the query parameter <code>type=aType</code> was given.
     *
     * @param type the <code>type name as String</code> to filter
     */
    fun getObjects(type: URI?, searchParams: List<ObjectSearchParameter> = emptyList()): Collection<Object> {
        return RequestBuilder
            .builder()
            .baseUrl(objectsApiProperties.objectsApi.url)
            .token(objectsApiProperties.objectsApi.token)
            .path("$rootUrlApiVersion/objects")
            .get()
            .queryParams(searchParams.associate { "data_attrs" to it.toQueryParameter() }.toMutableMap())
            .queryParam("type", type)
            .executeForCollection(Object::class.java)
    }

    /**
     * Retrieve an OBJECT and its actual RECORD.
     *
     * @param uuid the ID of the object
     */
    fun getObject(uuid: UUID): Object {
        return RequestBuilder
            .builder()
            .baseUrl(objectsApiProperties.objectsApi.url)
            .token(objectsApiProperties.objectsApi.token)
            .path("$rootUrlApiVersion/objects/$uuid")
            .get()
            .execute(Object::class.java)
    }

    /**
     * Retrieve an OBJECT and its actual RECORD where the data element gets mapped to the generic type.
     *
     * @param uuid the ID of the object
     * @param type the type that the data property of the Object should be deserialized to
     */
    fun <T> getTypedObject(uuid: UUID, type: ParameterizedTypeReference<GenericObject<T>>): GenericObject<T> {
        return RequestBuilder
            .builder()
            .baseUrl(objectsApiProperties.objectsApi.url)
            .token(objectsApiProperties.objectsApi.token)
            .path("$rootUrlApiVersion/objects/$uuid")
            .get()
            .execute(type)
    }

    /**
     * Deletes an OBJECT
     *
     * @param uuid the ID of the object
     */
    fun deleteObject(uuid: UUID) {
        RequestBuilder
            .builder()
            .baseUrl(objectsApiProperties.objectsApi.url)
            .token(objectsApiProperties.objectsApi.token)
            .path("$rootUrlApiVersion/objects/$uuid")
            .delete()
            .execute()
    }

    override fun getProperties(): ConnectorProperties {
        return objectsApiProperties
    }

    override fun setProperties(connectorProperties: ConnectorProperties) {
        objectsApiProperties = connectorProperties as ObjectsApiProperties
    }

    companion object {
        const val rootUrlApiVersion = "/api/v2"
        const val datePattern = "yyyy-MM-dd"
    }
}