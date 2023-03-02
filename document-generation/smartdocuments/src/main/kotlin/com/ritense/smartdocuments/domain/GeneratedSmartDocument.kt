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

package com.ritense.smartdocuments.domain

import com.ritense.documentgeneration.domain.GeneratedDocument
import java.util.Objects

data class GeneratedSmartDocument(
    private val name: String,
    private val extension: String,
    private val contentType: String,
    private val bytes: ByteArray,
) : GeneratedDocument {

    override fun getName(): String {
        return name
    }

    override fun getExtension(): String {
        return extension
    }

    override fun getSize(): Long {
        return bytes.size.toLong()
    }

    override fun getContentType(): String {
        return contentType
    }

    override fun getAsByteArray(): ByteArray {
        return bytes
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GeneratedSmartDocument

        if (name != other.name) return false
        if (extension != other.extension) return false
        if (contentType != other.contentType) return false

        return true
    }

    override fun hashCode(): Int {
        return Objects.hash(name, extension, contentType)
    }
}
