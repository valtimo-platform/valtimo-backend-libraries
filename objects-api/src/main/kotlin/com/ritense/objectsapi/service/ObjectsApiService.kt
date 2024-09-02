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


package com.ritense.objectsapi.service

import com.ritense.objectsapi.domain.GenericObject
import com.ritense.objectsapi.domain.Object
import com.ritense.objectsapi.domain.ObjectTypes
import com.ritense.objectsapi.domain.ResultWrapper
import com.ritense.objectsapi.domain.request.CreateObjectRequest
import com.ritense.objectsapi.domain.request.ModifyObjectRequest
import com.ritense.objectsapi.domain.request.ObjectSearchParameter
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import mu.KLogger
import mu.KotlinLogging
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Service
import java.net.URI
import java.util.UUID

@Service
@SkipComponentScan
class ObjectsApiService(
    protected var objectsApiProperties: ObjectsApiProperties,
) {

    /**
     * Retrieve a list of OBJECTTYPEs.
     */
    fun objectTypes(): Collection<ObjectTypes> {
        return RequestBuilder
            .builder()
            .baseUrl(objectsApiProperties.objectsTypeApi.url)
            .token(objectsApiProperties.objectsTypeApi.token)
            .path("${ObjectsApiConnector.rootUrlApiVersion}/objecttypes")
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
            .path("${ObjectsApiConnector.rootUrlApiVersion}/objects")
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
            .path("${ObjectsApiConnector.rootUrlApiVersion}/objects/${request.uuid}")
            .put()
            .body(request)
            .execute(Object::class.java)
    }

    /**
     * Retrieve a list of OBJECTs and their actual RECORD.
     * The actual record is defined as if the query parameter <code>type=aType</code> was given.
     * Do not use this method for querying the Objects-api from a Valtimo implementation.
     * Use getObjectsWrapped() instead.
     *
     * @param type the <code>type name as String</code> to filter
     */
    fun getObjects(type: URI?, searchParams: List<ObjectSearchParameter> = emptyList()): Collection<Object> {
        return buildList {
            var currentPage = 1
            while (true) {
                val result = getObjectsWrapped(type, searchParams, currentPage)
                addAll(result.results)

                if (result.next == null) break else currentPage++

                if (currentPage == 50) logger.warn {
                    "Retrieving over 50 object pages. Please consider using a paginated result!"
                }
            }
        }
    }

    @Deprecated("Marked for removal since 10.5.0")
    fun getObjectsWrapped(type: URI?, searchParams: List<ObjectSearchParameter> = emptyList()) {
        getObjectsWrapped(type, searchParams, null)
    }
    /**
     * Retrieve a Wrapper with a list of OBJECTs and their actual RECORD.
     * The actual record is defined as if the query parameter <code>type=aType</code> was given.
     * Use this method for querying the Objects-api from a Valtimo implementation
     *
     * @param type the <code>type name as String</code> to filter
     */
    fun getObjectsWrapped(type: URI?, searchParams: List<ObjectSearchParameter> = emptyList(), page:Int?): ResultWrapper<Object> {
        return RequestBuilder
            .builder()
            .baseUrl(objectsApiProperties.objectsApi.url)
            .token(objectsApiProperties.objectsApi.token)
            .path("${ObjectsApiConnector.rootUrlApiVersion}/objects")
            .get()
            .queryParams(searchParams.associate { "data_attrs" to it.toQueryParameter() }.toMutableMap())
            .queryParam("type", type)
            .queryParam("page", page)
            .executeWrapped(Object::class.java)
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
            .path("${ObjectsApiConnector.rootUrlApiVersion}/objects/$uuid")
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
            .path("${ObjectsApiConnector.rootUrlApiVersion}/objects/$uuid")
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
            .path("${ObjectsApiConnector.rootUrlApiVersion}/objects/$uuid")
            .delete()
            .execute()
    }

    companion object {
        private val logger: KLogger = KotlinLogging.logger {}
    }
}