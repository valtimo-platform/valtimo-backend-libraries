package com.ritense.objectsapi.service

import com.ritense.objectsapi.domain.GenericObject
import com.ritense.objectsapi.domain.Object
import com.ritense.objectsapi.domain.ObjectTypes
import com.ritense.objectsapi.domain.request.CreateObjectRequest
import com.ritense.objectsapi.domain.request.ModifyObjectRequest
import com.ritense.objectsapi.domain.request.ObjectSearchParameter
import java.net.URI
import java.util.UUID
import org.springframework.core.ParameterizedTypeReference

open class ObjectsApiService(
    protected open var objectsApiProperties: ObjectsApiProperties,
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
     *
     * @param type the <code>type name as String</code> to filter
     */
    fun getObjects(type: URI?, searchParams: List<ObjectSearchParameter> = emptyList()): Collection<Object> {
        return RequestBuilder
            .builder()
            .baseUrl(objectsApiProperties.objectsApi.url)
            .token(objectsApiProperties.objectsApi.token)
            .path("${ObjectsApiConnector.rootUrlApiVersion}/objects")
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
}