package com.ritense.besluit.service

import com.ritense.besluit.domain.Besluit
import com.ritense.besluit.domain.request.CreateBesluitRequest

open  class BesluitenService(
    protected open var besluitApiProperties: BesluitApiProperties,
) {
    /**
     * Retreive a list of BESLUITEN
     */
    fun besluiten(): Collection<Besluit> {
        return getRequestBuilder()
            .path("${BesluitConnector.rootUrlApiVersion}/besluiten")
            .get()
            .executeForCollection(Besluit::class.java)
    }

    /**
     * Create a BESLUIT
     *
     * @param request the <code>CreateBesluitRequest</code> to use when createing new requests
     */
    fun createBesluit(request: CreateBesluitRequest): Besluit {
        return getRequestBuilder()
            .path("${BesluitConnector.rootUrlApiVersion}/besluiten")
            .post()
            .execute(Besluit::class.java)
    }

    private fun getRequestBuilder(): RequestBuilder.Builder {
        return RequestBuilder
            .builder()
            .baseUrl(besluitApiProperties.besluitApi.url)
            .token(besluitApiProperties.besluitApi.secret)
    }
}