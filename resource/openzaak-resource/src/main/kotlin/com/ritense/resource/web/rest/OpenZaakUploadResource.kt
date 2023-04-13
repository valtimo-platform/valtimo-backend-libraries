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
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
import com.ritense.valtimo.contract.resource.Resource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api", produces = [APPLICATION_JSON_UTF8_VALUE])
class OpenZaakUploadResource(
    val resourceService: ResourceService
) {

    @PostMapping("/v1/resource/upload-open-zaak", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadOpenZaakFile(
        @RequestParam("file") file: MultipartFile,
        @RequestParam("documentDefinitionName") documentDefinitionName: String
    ): ResponseEntity<out Resource> {
        val storedResource: Resource =
            resourceService.store(documentDefinitionName, file.originalFilename!!, file)
        return ResponseEntity.ok(storedResource)
    }
}
