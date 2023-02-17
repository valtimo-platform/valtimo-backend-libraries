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

package com.ritense.zakenapi.web.rest.value

import com.ritense.document.domain.RelatedFile
import java.net.URI
import java.time.LocalDateTime
import java.util.UUID

class RelatedFileDto(
    val fileUrl: URI,
    private val fileName: String?,
    private val sizeInBytes: Long?,
    private val createdOn: LocalDateTime,
    private val createdBy: String
):  RelatedFile {
    override fun getFileId(): UUID {
        return UUID.fromString(fileUrl.path.substringAfterLast("/"))
    }

    override fun getFileName(): String? {
        return fileName
    }

    override fun getSizeInBytes(): Long? {
        return sizeInBytes
    }

    override fun getCreatedOn(): LocalDateTime {
        return createdOn
    }

    override fun getCreatedBy(): String {
        return createdBy
    }
}