/*
 * Copyright 2015-2020 Ritense BV, the Netherlands.
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

interface ResourceService {

    fun store(key: String, multipartFile: MultipartFile): Resource

    fun store(key: String, multipartFile: MultipartFile, fileStatus: FileStatus): Resource

    fun store(documentDefinitionName: String, name: String, multipartFile: MultipartFile): Resource

    fun store(key: String, fileUploadRequest: FileUploadRequest): Resource

    fun store(key: String, fileUploadRequest: FileUploadRequest, fileStatus: FileStatus): Resource

    fun getResourceUrl(id: UUID): ObjectUrlDTO

    fun getResourceUrl(fileName: String): URL

    fun getResourceContent(id: UUID): ObjectContentDTO

    fun removeResource(id: UUID)

    fun registerResource(resourceDTO: ResourceDTO): ResourceDTO

    fun removeResource(key: String)

    fun getResource(id: UUID): Resource

    fun getResourceByKey(fileName: String): Resource

    fun activate(id: UUID)

    fun pending(id: UUID)

}