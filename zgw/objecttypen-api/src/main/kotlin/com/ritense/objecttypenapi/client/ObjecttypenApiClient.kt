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

package com.ritense.objecttypenapi.client

import com.ritense.objecttypenapi.ObjecttypenApiAuthentication
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.toEntityList
import java.net.URI

class ObjecttypenApiClient(
    private val webclientBuilder: WebClient.Builder
) {

    fun getObjecttype(
        authentication: ObjecttypenApiAuthentication,
        objecttypeUrl: URI
    ): Objecttype {
        val url = if (objecttypeUrl.host == "host.docker.internal") {
            URI.create(objecttypeUrl.toString().replace("host.docker.internal", "localhost"))
        } else {
            objecttypeUrl
        }
        val result = webclientBuilder
            .clone()
            .filter(authentication)
            .build()
            .get()
            .uri(url)
            .retrieve()
            .toEntity(Objecttype::class.java)
            .block()

        return result?.body!!
    }

    fun getObjecttypes(
        authentication: ObjecttypenApiAuthentication,
        objecttypesUrl: URI
    ): List<Objecttype> {
        val url = if (objecttypesUrl.host == "host.docker.internal") {
            URI.create(objecttypesUrl.toString().replace("host.docker.internal", "localhost"))
        } else {
            objecttypesUrl
        }
        val result = webclientBuilder
            .clone()
            .filter(authentication)
            .build()
            .get()
            .uri(url)
            .retrieve()
            .toEntityList<Objecttype>()
            .block()

        return result?.body!!
    }
}