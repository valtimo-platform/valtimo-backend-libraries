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

package com.ritense.resource.service

import com.ritense.resource.BaseIntegrationTest
import com.ritense.resource.domain.MetadataType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TemporaryResourceServiceIntegrationTest : BaseIntegrationTest() {

    @Autowired
    lateinit var temporaryResourceService: TemporaryResourceService

    @Test
    fun `should store and get resource as inputStream`() {
        val fileData = "My file data"

        val resourceId = temporaryResourceService.store(fileData.byteInputStream())
        val result = temporaryResourceService.getResourceContentAsInputStream(resourceId)

        assertThat(result.reader().readText()).isEqualTo(fileData)
    }

    @Test
    fun `should store and retrieve metadata`() {
        val fileData = "My file data"
        val fileName = "test.txt"

        val resourceId = temporaryResourceService.store(
            fileData.byteInputStream(),
            mapOf(MetadataType.FILE_NAME.toString() to fileName)
        )

        val metadata = temporaryResourceService.getResourceMetadata(resourceId)
        assertThat(metadata[MetadataType.FILE_NAME.toString()]).isEqualTo(fileName)
    }

    @Test
    fun `should delete resource`() {
        val resourceId = temporaryResourceService.store("My file data".byteInputStream())

        val deleted = temporaryResourceService.deleteResource(resourceId)

        assertThat(deleted).isTrue
        val exception = assertThrows<IllegalArgumentException> {
            temporaryResourceService.getResourceContentAsInputStream(resourceId)
        }
        assertThat(exception.message).isEqualTo("No resource found with id '$resourceId'")
    }
}
