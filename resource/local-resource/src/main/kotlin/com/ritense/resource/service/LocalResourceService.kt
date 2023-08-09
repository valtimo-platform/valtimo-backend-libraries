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

package com.ritense.resource.service

import com.ritense.resource.service.request.FileUploadRequest
import com.ritense.resource.web.ObjectContentDTO
import com.ritense.resource.web.ObjectUrlDTO
import com.ritense.resource.web.ResourceDTO
import com.ritense.valtimo.contract.resource.FileStatus
import com.ritense.valtimo.contract.resource.Resource
import org.springframework.web.multipart.MultipartFile
import java.net.URL
import java.util.UUID

class LocalResourceService : ResourceService {

    override fun store(key: String, multipartFile: MultipartFile): Resource {
        TODO(NOT_YET_IMPLEMENTED_MSG)
    }

    override fun store(key: String, multipartFile: MultipartFile, fileStatus: FileStatus): Resource {
        TODO(NOT_YET_IMPLEMENTED_MSG)
    }

    override fun store(documentDefinitionName: String, name: String, multipartFile: MultipartFile): Resource {
        TODO(NOT_YET_IMPLEMENTED_MSG)
    }

    override fun store(key: String, fileUploadRequest: FileUploadRequest): Resource {
        TODO(NOT_YET_IMPLEMENTED_MSG)
    }

    override fun store(key: String, fileUploadRequest: FileUploadRequest, fileStatus: FileStatus): Resource {
        TODO(NOT_YET_IMPLEMENTED_MSG)
    }

    override fun getResourceUrl(id: UUID): ObjectUrlDTO {
        TODO(NOT_YET_IMPLEMENTED_MSG)
    }

    override fun getResourceUrl(fileName: String): URL {
        TODO(NOT_YET_IMPLEMENTED_MSG)
    }

    override fun getResourceContent(id: UUID): ObjectContentDTO {
        TODO(NOT_YET_IMPLEMENTED_MSG)
    }

    override fun removeResource(id: UUID) {
        TODO(NOT_YET_IMPLEMENTED_MSG)
    }

    override fun removeResource(key: String) {
        TODO(NOT_YET_IMPLEMENTED_MSG)
    }

    override fun registerResource(resourceDTO: ResourceDTO): ResourceDTO {
        TODO(NOT_YET_IMPLEMENTED_MSG)
    }

    override fun getResource(id: UUID): Resource {
        TODO(NOT_YET_IMPLEMENTED_MSG)
    }

    override fun getResourceByKey(fileName: String): Resource {
        TODO(NOT_YET_IMPLEMENTED_MSG)
    }

    override fun activate(id: UUID) {
        TODO(NOT_YET_IMPLEMENTED_MSG)
    }

    override fun pending(id: UUID) {
        TODO(NOT_YET_IMPLEMENTED_MSG)
    }

    companion object {
        private const val NOT_YET_IMPLEMENTED_MSG = "Not yet implemented"
    }
}