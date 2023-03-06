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

package com.ritense.resource.service.request

import org.apache.commons.io.FilenameUtils
import org.springframework.web.multipart.MultipartFile
import java.io.InputStream

class MultipartFileUploadRequest(
    private val name: String,
    private val extension: String,
    private val size: Long,
    private val contentType: String,
    private val inputStream: InputStream
) : FileUploadRequest {

    override fun getName(): String {
        return name
    }

    override fun getExtension(): String {
        return extension
    }

    override fun getSize(): Long {
        return size
    }

    override fun getContentType(): String {
        return contentType
    }

    override fun getInputStream(): InputStream {
        return inputStream
    }

    companion object Factory {

        fun from(multipartFile: MultipartFile): MultipartFileUploadRequest {
            return MultipartFileUploadRequest(
                multipartFile.name,
                FilenameUtils.getExtension(multipartFile.originalFilename),
                multipartFile.size,
                multipartFile.contentType!!,
                multipartFile.inputStream
            )
        }
    }

}