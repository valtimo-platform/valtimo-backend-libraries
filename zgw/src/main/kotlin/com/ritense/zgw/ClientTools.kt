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

package com.ritense.zgw

import org.springframework.core.ParameterizedTypeReference
import org.springframework.core.ResolvableType
import org.springframework.web.util.UriBuilder
import java.net.URI

class ClientTools {
    companion object {
        fun <T> getTypedPage(responseClass: Class<out T>): ParameterizedTypeReference<Page<T>> {
            return ParameterizedTypeReference.forType(
                ResolvableType.forClassWithGenerics(Page::class.java, responseClass).type
            )
        }

        fun baseUrlToBuilder(builder: UriBuilder, uri: URI): UriBuilder {
            return builder.scheme(uri.scheme)
                .host(uri.host)
                .path(uri.path)
                .port(uri.port)
        }
    }
}