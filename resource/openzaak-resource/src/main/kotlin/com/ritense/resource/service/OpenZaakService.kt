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

package com.ritense.resource.service

import com.ritense.openzaak.service.DocumentenService
import com.ritense.openzaak.service.impl.model.documenten.InformatieObject
import com.ritense.resource.domain.OpenZaakResource
import com.ritense.resource.domain.ResourceId
import com.ritense.resource.repository.OpenZaakResourceRepository
import com.ritense.resource.service.request.FileUploadRequest
import com.ritense.resource.service.request.MultipartFileUploadRequest
import com.ritense.resource.web.ObjectContentDTO
import com.ritense.resource.web.ObjectUrlDTO
import com.ritense.resource.web.ResourceDTO
import com.ritense.valtimo.contract.resource.FileStatus
import com.ritense.valtimo.contract.resource.Resource
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.multipart.MultipartFile
import java.net.URI
import java.net.URL
import java.time.LocalDateTime
import java.util.UUID

@Deprecated("Since 12.0.0. Replaced by Documenten API module.")
class OpenZaakService(
    val documentenService: DocumentenService,
    val openZaakResourceRepository: OpenZaakResourceRepository,
    val request: HttpServletRequest
): ResourceService {

    @Deprecated("Since 12.0.0")
    override fun store(key: String, multipartFile: MultipartFile): Resource {
        throw NotImplementedError()
    }

    @Deprecated("Since 12.0.0")
    override fun store(key: String, multipartFile: MultipartFile, fileStatus: FileStatus): Resource {

        val informatieObjectUrl =
            documentenService.createEnkelvoudigInformatieObject(key, multipartFile)

        val uploadRequest = MultipartFileUploadRequest.from(multipartFile)

        val openZaakResource = OpenZaakResource(
            ResourceId.newId(UUID.randomUUID()),
            informatieObjectUrl,
            key,
            uploadRequest.getExtension(),
            uploadRequest.getSize(),
            LocalDateTime.now()
        )
        return openZaakResourceRepository.saveAndFlush(openZaakResource)
    }

    @Deprecated("Since 12.0.0")
    override fun store(documentDefinitionName: String, name: String, multipartFile: MultipartFile): OpenZaakResource {
        val informatieObjectUrl =
            documentenService.createEnkelvoudigInformatieObject(documentDefinitionName, multipartFile)

        val uploadRequest = MultipartFileUploadRequest.from(multipartFile)

        val openZaakResource = OpenZaakResource(
            ResourceId.newId(UUID.randomUUID()),
            informatieObjectUrl,
            name,
            uploadRequest.getExtension(),
            uploadRequest.getSize(),
            LocalDateTime.now()
        )
        return openZaakResourceRepository.saveAndFlush(openZaakResource)
    }

    @Deprecated("Since 12.0.0")
    fun store(informatieObject: InformatieObject): OpenZaakResource {
        val openZaakResource = OpenZaakResource(
            ResourceId.newId(UUID.randomUUID()),
            informatieObject.url,
            informatieObject.bestandsnaam,
            informatieObject.bestandsnaam.substringAfterLast("."),
            informatieObject.bestandsomvang,
            informatieObject.beginRegistratie
        )
        return openZaakResourceRepository.save(openZaakResource)
    }

    @Deprecated("Since 12.0.0")
    override fun store(key: String, fileUploadRequest: FileUploadRequest): Resource {
        throw NotImplementedError()
    }

    @Deprecated("Since 12.0.0")
    override fun store(key: String, fileUploadRequest: FileUploadRequest, fileStatus: FileStatus): Resource {
        throw NotImplementedError()
    }

    @Deprecated("Since 12.0.0")
    override fun getResourceUrl(id: UUID): ObjectUrlDTO {
        val resource = openZaakResourceRepository.getReferenceById(ResourceId.existingId(id))

        return ObjectUrlDTO(
            getDownloadUrl(resource),
            ResourceDTO(
                resource.id.id.toString(),
                resource.name(),
                resource.name(),
                resource.extension,
                resource.sizeInBytes,
                resource.createdOn
            )
        )
    }

    @Deprecated("Since 12.0.0")
    override fun getResourceUrl(fileName: String): URL {
        throw NotImplementedError()
    }

    @Deprecated("Since 12.0.0")
    override fun getResourceContent(id: UUID): ObjectContentDTO {
        val resource = openZaakResourceRepository.getReferenceById(ResourceId.existingId(id))
        return ObjectContentDTO(
            getDownloadUrl(resource),
            ResourceDTO(
                resource.id.id.toString(),
                resource.name(),
                resource.name(),
                resource.extension,
                resource.sizeInBytes,
                resource.createdOn
            ),
            documentenService.getObjectInformatieObject(resource.informatieObjectUrl)
        )
    }

    @Deprecated("Since 12.0.0")
    override fun removeResource(id: UUID) {
        throw NotImplementedError()
    }

    @Deprecated("Since 12.0.0")
    override fun removeResource(key: String) {
        throw NotImplementedError()
    }

    @Deprecated("Since 12.0.0")
    override fun registerResource(resourceDTO: ResourceDTO): ResourceDTO {
        throw NotImplementedError()
    }

    @Deprecated("Since 12.0.0")
    override fun getResource(id: UUID): OpenZaakResource {
        return openZaakResourceRepository.findById(ResourceId.existingId(id)).orElseThrow()
    }

    @Deprecated("Since 12.0.0")
    override fun getResourceByKey(fileName: String): Resource {
        throw NotImplementedError()
    }

    @Deprecated("Since 12.0.0")
    override fun activate(id: UUID) {
        throw NotImplementedError()
    }

    @Deprecated("Since 12.0.0")
    override fun pending(id: UUID) {
        throw NotImplementedError()
    }

    @Deprecated("Since 12.0.0")
    fun getResourceByInformatieObjectUrl(url: URI): OpenZaakResource {
        return openZaakResourceRepository.findByInformatieObjectUrl(url)
    }

    private fun getDownloadUrl(resource: OpenZaakResource): URL {
        val currentRequestUrl = URL(request.requestURL.toString())
        return URL(currentRequestUrl, "/api/v1/resource/${resource.resourceId.id}/download")
    }

    companion object {
        private const val NOT_YET_IMPLEMENTED_MSG = "Not yet implemented"
    }
}