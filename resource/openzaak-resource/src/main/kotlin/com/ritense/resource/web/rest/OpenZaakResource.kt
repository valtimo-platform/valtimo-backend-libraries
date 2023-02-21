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

package com.ritense.resource.web.rest

import com.ritense.resource.service.ResourceService
import com.ritense.resource.web.ObjectUrlDTO
import com.ritense.resource.web.ResourceDTO
import com.ritense.valtimo.contract.resource.Resource
import java.net.URLConnection
import java.util.UUID
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile

class OpenZaakResource(
    val resourceService: ResourceService
) : ResourceResource {

    override fun get(resourceId: String): ResponseEntity<ObjectUrlDTO> {
        return ResponseEntity.ok(resourceService.getResourceUrl(UUID.fromString(resourceId)))
    }

    override fun getContent(resourceId: String): ResponseEntity<ByteArray> {
        val resourceContent = resourceService.getResourceContent(UUID.fromString(resourceId))

        // try to guess content type for file
        var fileMediaType: MediaType;
        try {
            val contentType = URLConnection.guessContentTypeFromName(resourceContent.resource.name)
            fileMediaType = MediaType.valueOf(contentType)
        } catch(exception: RuntimeException) {
            // when unable to determine media type default to application/octet-stream
            fileMediaType = MediaType.APPLICATION_OCTET_STREAM
        }

        return ResponseEntity.ok()
            .contentType(fileMediaType)
            .body(resourceContent.content)
    }

    override fun register(resourceDTO: ResourceDTO): ResponseEntity<ResourceDTO> {
        TODO("Not yet implemented") //enkelvoudig informatie object oz + resource db record
    }

    override fun delete(resourceId: String): ResponseEntity<Void> {
        TODO("Not yet implemented")
    }
}