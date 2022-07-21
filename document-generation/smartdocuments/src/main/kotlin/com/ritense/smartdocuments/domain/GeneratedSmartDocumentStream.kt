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

package com.ritense.smartdocuments.domain

import java.io.InputStream
import java.util.Objects

data class GeneratedSmartDocumentStream(
    val name: String,
    val extension: String,
    val contentType: String,
    val data: InputStream,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GeneratedSmartDocumentStream

        if (name != other.name) return false
        if (extension != other.extension) return false
        if (contentType != other.contentType) return false

        return true
    }

    override fun hashCode(): Int {
        return Objects.hash(name, extension, contentType)
    }
}
