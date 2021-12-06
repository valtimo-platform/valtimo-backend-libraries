package com.ritense.resource.web.rest

import com.ritense.resource.service.ResourceService
import com.ritense.valtimo.contract.resource.Resource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping(value = ["/api/resource"], produces = [MediaType.APPLICATION_JSON_VALUE])
class OpenZaakUploadResource(
    val resourceService: ResourceService
) {

    @PostMapping(value = ["/upload-open-zaak"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadOpenZaakFile(
        @RequestParam("file") file: MultipartFile,
        @RequestParam("documentDefinitionName") documentDefinitionName: String
    ): ResponseEntity<out Resource> {
        val storedResource: Resource =
            resourceService.store(documentDefinitionName, file.originalFilename!!, file)
        return ResponseEntity.ok(storedResource)
    }
}