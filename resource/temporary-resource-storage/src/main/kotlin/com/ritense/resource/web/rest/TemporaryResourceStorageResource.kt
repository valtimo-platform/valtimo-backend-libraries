/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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
import com.ritense.valtimo.contract.utils.SecurityUtils
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/resource/temp")
class TemporaryResourceStorageResource(
    private val resourceService: TemporaryResourceStorageService,
    private val applicationEventPublisher: ApplicationEventPublisher,
) {

    @PostMapping(consumes = [MULTIPART_FORM_DATA_VALUE])
    fun uploadFileWithMetadata(
        @RequestParam("file") file: MultipartFile,
        @RequestParam metaData: Map<String, Any>,
    ): ResponseEntity<Void> {

        val mutableMetaData = metaData.toMutableMap()
        file.originalFilename?.let { mutableMetaData.putIfAbsent(MetadataType.FILE_NAME.key, it) }
        file.contentType?.let { mutableMetaData.putIfAbsent(MetadataType.CONTENT_TYPE.key, it) }
        SecurityUtils.getCurrentUserLogin()?.let { mutableMetaData.putIfAbsent(MetadataType.USER.key, it) }

        val resourceId = resourceService.store(file.inputStream, mutableMetaData)
        applicationEventPublisher.publishEvent(TemporaryResourceUploadedEvent(resourceId))

        return ResponseEntity.noContent().build() // Don't respond with resourceId because the resourceId reveals the local file structure.
    }
}
