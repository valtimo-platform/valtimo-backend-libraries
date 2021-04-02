package com.ritense.resource.web.rest

import com.ritense.resource.domain.OpenZaakResource
import com.ritense.resource.service.OpenZaakService
import com.ritense.resource.web.ObjectUrlDTO
import com.ritense.resource.web.ResourceDTO
import com.ritense.valtimo.contract.resource.Resource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile

class OpenZaakResource(
    val openZaakService: OpenZaakService
) : ResourceResource {

    @PostMapping(value = ["/resource/upload-open-zaak"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadOpenZaakFile(
        @RequestParam("file") file: MultipartFile,
        @RequestParam("documentDefinitionName") documentDefinitionName: String
    ): ResponseEntity<out Resource> {
        val storedResource: OpenZaakResource =
            openZaakService.store(documentDefinitionName, file.originalFilename!!, file)
        return ResponseEntity.ok(storedResource)
    }

    override fun get(resourceId: String): ResponseEntity<ObjectUrlDTO> {
        TODO("Not yet implemented") //valtimo url
    }

    override fun register(resourceDTO: ResourceDTO): ResponseEntity<ResourceDTO> {
        TODO("Not yet implemented") //enkelvoudig informatie object oz + resource db record
    }

    override fun delete(resourceId: String): ResponseEntity<Void> {
        TODO("Not yet implemented")
    }
}