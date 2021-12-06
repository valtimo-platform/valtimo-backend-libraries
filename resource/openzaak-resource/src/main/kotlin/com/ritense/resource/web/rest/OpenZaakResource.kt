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