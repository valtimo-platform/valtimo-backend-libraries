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

package com.ritense.resource.service

import com.ritense.resource.service.TemporaryResourceStorageService.Companion.TEMP_DIR
import mu.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit
import kotlin.io.path.deleteIfExists
import kotlin.io.path.listDirectoryEntries

open class TemporaryResourceStorageDeletionService(
    private val retentionInMinutes: Long,
    private val tempDir: Path = TEMP_DIR,
) {

    @Scheduled(
        fixedRateString = "\${valtimo.temporaryResourceStorage.retentionInMinutes:60}",
        timeUnit = TimeUnit.MINUTES
    )
    open fun deleteOldTemporaryResources() {

        tempDir.listDirectoryEntries().forEach { file ->
            try {
                val fileCreationTime = Files.readAttributes(file, BasicFileAttributes::class.java).creationTime()

                if (fileCreationTime.toInstant().plus(Duration.ofMinutes(retentionInMinutes)) < Instant.now()) {
                    file.deleteIfExists()
                }
            } catch (e: Exception) {
                logger.error(e) { "Failed to delete temporary resource" }
            }
        }
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}
