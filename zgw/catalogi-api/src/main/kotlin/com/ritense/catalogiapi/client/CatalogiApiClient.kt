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

package com.ritense.catalogiapi.client

import com.ritense.catalogiapi.CatalogiApiAuthentication
import com.ritense.catalogiapi.domain.Besluittype
import com.ritense.catalogiapi.domain.Informatieobjecttype
import com.ritense.catalogiapi.domain.Resultaattype
import com.ritense.catalogiapi.domain.Roltype
import com.ritense.catalogiapi.domain.Statustype
import com.ritense.catalogiapi.domain.ZaaktypeInformatieobjecttype
import com.ritense.zgw.ClientTools
import com.ritense.zgw.Page
import mu.KotlinLogging
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.Cacheable
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriBuilder
import java.net.URI

open class CatalogiApiClient(
    private val webclientBuilder: WebClient.Builder,
    private val cacheManager: CacheManager
) {
    open fun getZaaktypeInformatieobjecttypes(
        authentication: CatalogiApiAuthentication,
        baseUrl: URI,
        request: ZaaktypeInformatieobjecttypeRequest
    ): Page<ZaaktypeInformatieobjecttype> {
        validateUrlHost(baseUrl, request.zaaktype)
        val result = buildWebclient(authentication)
            .get()
            .uri {
                ClientTools.baseUrlToBuilder(it, baseUrl)
                    .pathSegment("zaaktype-informatieobjecttypen")
                    .addOptionalQueryParamFromRequest("zaaktype", request.zaaktype)
                    .addOptionalQueryParamFromRequest("informatieobjecttype", request.informatieobjecttype)
                    .addOptionalQueryParamFromRequest("richting", request.richting?.getSearchValue())
                    .addOptionalQueryParamFromRequest("status", request.status?.getSearchValue())
                    .addOptionalQueryParamFromRequest("page", request.page)
                    .build()
            }
            .retrieve()
            .toEntity(ClientTools.getTypedPage(ZaaktypeInformatieobjecttype::class.java))
            .block()

        return result?.body!!
    }

    open fun getInformatieobjecttypes(
        authentication: CatalogiApiAuthentication,
        baseUrl: URI,
        request: InformatieobjecttypeRequest
    ): Page<Informatieobjecttype> {
        val result = buildWebclient(authentication)
            .get()
            .uri {
                ClientTools.baseUrlToBuilder(it, baseUrl)
                    .pathSegment("informatieobjecttypen")
                    .addOptionalQueryParamFromRequest("status", request.status?.getSearchValue())
                    .addOptionalQueryParamFromRequest("page", request.page)
                    .build()
            }.retrieve()
            .toEntity(ClientTools.getTypedPage(Informatieobjecttype::class.java))
            .block()

        return result?.body!!
    }

    @Cacheable(INFORMATIEOBJECTTYPECACHE_KEY, key = "#informatieobjecttypeUrl")
    open fun getInformatieobjecttype(
        authentication: CatalogiApiAuthentication,
        baseUrl: URI,
        informatieobjecttypeUrl: URI
    ): Informatieobjecttype {
        validateUrlHost(baseUrl, informatieobjecttypeUrl)
        val result = buildWebclient(authentication)
            .get()
            .uri(informatieobjecttypeUrl)
            .retrieve()
            .toEntity(Informatieobjecttype::class.java)
            .block()

        return result?.body!!
    }

    open fun getRoltypen(
        authentication: CatalogiApiAuthentication,
        baseUrl: URI,
        request: RoltypeRequest,
    ): Page<Roltype> {
        validateUrlHost(baseUrl, request.zaaktype)
        val result = buildWebclient(authentication)
            .get()
            .uri {
                ClientTools.baseUrlToBuilder(it, baseUrl)
                    .pathSegment("roltypen")
                    .addOptionalQueryParamFromRequest("zaaktype", request.zaaktype)
                    .addOptionalQueryParamFromRequest("omschrijvingGeneriek", request.omschrijvingGeneriek)
                    .addOptionalQueryParamFromRequest("status", request.status?.getSearchValue())
                    .addOptionalQueryParamFromRequest("page", request.page)
                    .build()
            }.retrieve()
            .toEntity(ClientTools.getTypedPage(Roltype::class.java))
            .block()

        return result?.body!!
    }

    open fun getStatustype(
        authentication: CatalogiApiAuthentication,
        baseUrl: URI,
        informatieobjecttypeUrl: URI
    ): Statustype {
        validateUrlHost(baseUrl, informatieobjecttypeUrl)
        val result = buildWebclient(authentication)
            .get()
            .uri(informatieobjecttypeUrl)
            .retrieve()
            .toEntity(Statustype::class.java)
            .block()

        return result?.body!!
    }

    open fun getStatustypen(
        authentication: CatalogiApiAuthentication,
        baseUrl: URI,
        request: StatustypeRequest,
    ): Page<Statustype> {
        validateUrlHost(baseUrl, request.zaaktype)
        val result = buildWebclient(authentication)
            .get()
            .uri {
                ClientTools.baseUrlToBuilder(it, baseUrl)
                    .pathSegment("statustypen")
                    .addOptionalQueryParamFromRequest("zaaktype", request.zaaktype)
                    .addOptionalQueryParamFromRequest("status", request.status?.getSearchValue())
                    .addOptionalQueryParamFromRequest("page", request.page)
                    .build()
            }.retrieve()
            .toEntity(ClientTools.getTypedPage(Statustype::class.java))
            .block()

        return result?.body!!
    }

    open fun getResultaattypen(
        authentication: CatalogiApiAuthentication,
        baseUrl: URI,
        request: ResultaattypeRequest,
    ): Page<Resultaattype> {
        validateUrlHost(baseUrl, request.zaaktype)
        val result = buildWebclient(authentication)
            .get()
            .uri {
                ClientTools.baseUrlToBuilder(it, baseUrl)
                    .pathSegment("resultaattypen")
                    .addOptionalQueryParamFromRequest("zaaktype", request.zaaktype)
                    .addOptionalQueryParamFromRequest("status", request.status?.getSearchValue())
                    .addOptionalQueryParamFromRequest("page", request.page)
                    .build()
            }.retrieve()
            .toEntity(ClientTools.getTypedPage(Resultaattype::class.java))
            .block()

        return result?.body!!
    }

    open fun getBesluittypen(
        authentication: CatalogiApiAuthentication,
        baseUrl: URI,
        request: BesluittypeRequest,
    ): Page<Besluittype> {
        validateUrlHost(baseUrl, request.zaaktypen)
        val result = buildWebclient(authentication)
            .get()
            .uri {
                ClientTools.baseUrlToBuilder(it, baseUrl)
                    .pathSegment("besluittypen")
                    .addOptionalQueryParamFromRequest("catalogus", request.catalogus)
                    .addOptionalQueryParamFromRequest("zaaktypen", request.zaaktypen)
                    .addOptionalQueryParamFromRequest("informatieobjecttypen", request.informatieobjecttypen)
                    .addOptionalQueryParamFromRequest("status", request.status?.getSearchValue())
                    .addOptionalQueryParamFromRequest("page", request.page)
                    .build()
            }.retrieve()
            .toEntity(ClientTools.getTypedPage(Besluittype::class.java))
            .block()

        return result?.body!!
    }

    open fun prefillCache(authenticationPluginConfiguration: CatalogiApiAuthentication, url: URI) {
        prefillInformatieobjecttypeCache(authenticationPluginConfiguration, url)
    }

    private fun prefillInformatieobjecttypeCache(authenticationPluginConfiguration: CatalogiApiAuthentication, url: URI) {
        var currentPage = 1
        var currentResults: Page<Informatieobjecttype>?

        do {
            logger.debug { "Getting page of informatieobjecttypes, page $currentPage for catalogi api $url" }
            currentResults = getInformatieobjecttypes(
                authenticationPluginConfiguration,
                url,
                InformatieobjecttypeRequest(
                    status = InformatieobjecttypePublishedStatus.DEFINITIEF,
                    page = currentPage++
                )
            )
            currentResults.results.forEach {
                cacheManager.getCache(INFORMATIEOBJECTTYPECACHE_KEY)?.put(it.url, it)
            }
        } while(currentResults?.next != null)
    }

    private fun validateUrlHost(baseUrl: URI, url: URI?) {
        if (url != null && baseUrl.host != url.host) {
            throw IllegalArgumentException(
                "Requested url '$url' is not valid for baseUrl '$baseUrl'"
            )
        }
    }

    private fun buildWebclient(authentication: CatalogiApiAuthentication): WebClient {
        return webclientBuilder
            .clone()
            .filter(authentication)
            .build()
    }

    private fun UriBuilder.addOptionalQueryParamFromRequest(name: String, value: Any?): UriBuilder {
        if (value != null)
            this.queryParam(name, value.toString())
        return this
    }

    companion object {
        val logger = KotlinLogging.logger {}
        const val INFORMATIEOBJECTTYPECACHE_KEY = "zgw-catalogiapi-informatieobjecttype"
    }
}
