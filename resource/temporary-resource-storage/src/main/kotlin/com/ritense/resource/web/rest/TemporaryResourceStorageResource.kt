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

package com.ritense.resource.web.rest

import com.ritense.resource.domain.MetadataType
import com.ritense.resource.domain.TemporaryResourceUploadedEvent
import com.ritense.resource.service.TemporaryResourceStorageService
import com.ritense.resource.web.rest.response.ResourceDto
import com.ritense.resource.web.rest.response.StorageMetadataValue
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
import com.ritense.valtimo.contract.utils.SecurityUtils
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@SkipComponentScan
@RequestMapping("/api", produces = [APPLICATION_JSON_UTF8_VALUE])
class TemporaryResourceStorageResource(
    private val resourceService: TemporaryResourceStorageService,
    private val applicationEventPublisher: ApplicationEventPublisher,
) {

    @PostMapping("/v1/resource/temp", consumes = [MULTIPART_FORM_DATA_VALUE])
    fun uploadFileWithMetadata(
        @RequestParam("file") file: MultipartFile,
        @RequestParam metaData: Map<String, Any>,
    ): ResponseEntity<ResourceDto> {

        val mutableMetaData = metaData.toMutableMap()
        file.originalFilename?.let { mutableMetaData.putIfAbsent(MetadataType.FILE_NAME.key, it) }
        file.contentType?.let { mutableMetaData.putIfAbsent(MetadataType.CONTENT_TYPE.key, it) }
        SecurityUtils.getCurrentUserLogin()?.let { mutableMetaData.putIfAbsent(MetadataType.USER.key, it) }

        val resourceId = resourceService.store(file.inputStream, mutableMetaData)
        applicationEventPublisher.publishEvent(TemporaryResourceUploadedEvent(resourceId))

        return ResponseEntity.ok(
            ResourceDto(
                resourceId,
                mutableMetaData[MetadataType.FILE_NAME.key] as String?,
                file.size
            )
        )
    }

    @GetMapping("/v1/resource-storage/{resourceStorageFieldId}/metadata/{metadataKey}")
    fun getMetadataValue(
        @PathVariable("resourceStorageFieldId") resourceStorageFieldId: String,
        @PathVariable("metadataKey") metadataKey: String,
    ) :ResponseEntity<StorageMetadataValue> {
        val metadataValue = resourceService.getMetadataValue(resourceStorageFieldId, metadataKey)

        return ResponseEntity.ok(
            StorageMetadataValue(metadataValue)
        )
    }
}
