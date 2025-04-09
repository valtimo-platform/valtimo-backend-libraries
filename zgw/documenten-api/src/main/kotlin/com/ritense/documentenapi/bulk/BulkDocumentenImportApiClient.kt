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

package com.ritense.documentenapi.bulk

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.authorization.AuthorizationService
import com.ritense.authorization.request.EntityAuthorizationRequest
import com.ritense.documentenapi.DocumentenApiAuthentication
import com.ritense.outbox.OutboxService
import com.ritense.resource.authorization.ResourcePermission
import com.ritense.resource.authorization.ResourcePermissionActionProvider
import com.ritense.zgw.ClientTools
import mu.KotlinLogging
import org.springframework.core.io.InputStreamResource
import org.springframework.core.io.Resource
import org.springframework.http.MediaType
import org.springframework.http.converter.ResourceHttpMessageConverter
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import java.io.InputStream
import java.net.URI

/**
 * ApiClient specifically for the bulk import of documents which is added as experimental feature to Open-Zaak.
 */
class BulkDocumentenImportApiClient(
    private val restClientBuilder: RestClient.Builder,
    private val outboxService: OutboxService,
    private val objectMapper: ObjectMapper,
    private val authorizationService: AuthorizationService,
    private val authorizationEnabled: Boolean = false,
) {

    fun createImport(
        authentication: DocumentenApiAuthentication,
        baseUrl: URI,
    ): CreateImportResult {
        checkPermission()
        val result = restClient(authentication)
            .post()
            .uri {
                ClientTools.baseUrlToBuilder(it, baseUrl)
                    .pathSegment("import")
                    .pathSegment("create")
                    .build()
            }
            .contentType(MediaType.APPLICATION_JSON)
            .retrieve()
            .body<CreateImportResult>()!!

        outboxService.send { DocumentImportCreated(result.statusUrl, objectMapper.valueToTree(result)) }
        return result
    }

    fun getImportStatus(
        authentication: DocumentenApiAuthentication,
        importUuidOrUrl: String,
        /**
         * Required if the {@param importUuidOrUrl} is a UUID.
         */
        baseUrl: URI? = null,
    ): DocumentImportStatus {
        checkPermission()

        val result = restClient(authentication)
            .get()
            .uri {
                if (baseUrl != null)
                    ClientTools.baseUrlToBuilder(it, baseUrl)
                        .pathSegment("import")
                        .pathSegment(importUuidOrUrl)
                        .pathSegment("status")
                        .build()
                else URI.create(importUuidOrUrl)
            }
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .body<DocumentImportStatus>()!!
        return result
    }

    /**
     * Returns an InputStream of the raw text/csv so that you can process it in a streaming way.
     */
    fun getImportReport(
        authentication: DocumentenApiAuthentication,
        importUuidOrUrl: String,
        /**
         * Required if the {@param importUuidOrUrl} is a UUID.
         */
        baseUrl: URI? = null,
    ): InputStream {

        checkPermission()

        return restClient(authentication)
            .get()
            .uri {
                if (baseUrl != null)
                    ClientTools.baseUrlToBuilder(it, baseUrl)
                        .pathSegment("import")
                        .pathSegment(importUuidOrUrl)
                        .pathSegment("report")
                        .build()
                else URI.create(importUuidOrUrl)
            }
            .accept(MediaType.parseMediaType("text/csv"))
            .retrieve()
            .body(Resource::class.java)!!.inputStream
    }

    /**
     * Allows for uploading a CSV using an InputStream.
     */
    fun uploadImportCsv(
        authentication: DocumentenApiAuthentication,
        csv: InputStream,
        importUuidOrUrl: String,
        baseUrl: URI? = null,
    ) {

        checkPermission()
        restClient(authentication)
            .post()
            .uri {
                if (baseUrl != null)
                    ClientTools.baseUrlToBuilder(it, baseUrl)
                        .pathSegment("import")
                        .pathSegment(importUuidOrUrl)
                        .build()
                else URI.create(importUuidOrUrl)
            }
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(InputStreamResource(csv))
            .retrieve()
            .toBodilessEntity()
    }

    private fun checkPermission() {
        if (authorizationEnabled) {
            authorizationService.requirePermission(
                EntityAuthorizationRequest(
                    ResourcePermission::class.java,
                    ResourcePermissionActionProvider.IMPORT,
                    ResourcePermission()
                )
            )
        }
    }

    private fun restClient(authentication: DocumentenApiAuthentication): RestClient {
        return restClientBuilder
            .clone()
            .apply {
                authentication.applyAuth(it)
            }
            .messageConverters {
                it + ResourceHttpMessageConverter(true)
            }
            .build()
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}
