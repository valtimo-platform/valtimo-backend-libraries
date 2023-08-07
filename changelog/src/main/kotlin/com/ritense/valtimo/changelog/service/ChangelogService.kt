/*
 *  Copyright 2015-2023 Ritense BV, the Netherlands.
 *
 *  Licensed under EUPL, Version 1.2 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.ritense.valtimo.changelog.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.valtimo.changelog.domain.Changeset
import com.ritense.valtimo.changelog.repository.ChangesetRepository
import liquibase.util.MD5Util
import liquibase.util.StringUtil
import mu.KotlinLogging
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.ResourcePatternUtils
import org.springframework.transaction.annotation.Transactional
import java.io.IOException
import java.text.Normalizer
import java.time.Instant
import kotlin.jvm.optionals.getOrElse

@Transactional
class ChangelogService(
    private val resourceLoader: ResourceLoader,
    private val changesetRepository: ChangesetRepository,
    private val objectMapper: ObjectMapper,
) {

    fun getFilename(resource: Resource) = resource.uri.toASCIIString().substringAfterLast("resources/")

    fun saveChangeset(changesetId: String, key: String?, filename: String, md5sum: String) {
        changesetRepository.save(
            Changeset(
                id = changesetId,
                key = key,
                filename = filename,
                dateExecuted = Instant.now(),
                orderExecuted = changesetRepository.getNextOrderExecuted() ?: 0,
                md5sum = md5sum
            )
        )
    }

    fun deleteChangesetsByKey(key: String?) = changesetRepository.deleteAllByKey(key)

    fun isNewValidChangeset(changesetId: String, md5sum: String): Boolean {
        val existing = changesetRepository.findById(changesetId).getOrElse {
            return true
        }
        if (md5sum != existing.md5sum) {
            throw RuntimeException("Computed checksum '$md5sum' doesn't match existing '${existing.md5sum}' for ${existing.filename}")
        } else {
            logger.debug { "Verified checksum '$md5sum' for ${existing.filename}" }
        }
        return false
    }

    fun computeMd5sum(valueToChecksum: Any) = computeMd5sum(objectMapper.writeValueAsString(valueToChecksum))

    fun computeMd5sum(valueToChecksum: String): String {
        return MD5Util.computeMD5(
            Normalizer.normalize(
                StringUtil.standardizeLineEndings(valueToChecksum)
                    .replace("\uFFFD", ""), Normalizer.Form.NFC
            )
        )
    }

    @Throws(IOException::class)
    fun loadResources(path: String): List<Resource> = ResourcePatternUtils.getResourcePatternResolver(resourceLoader)
        .getResources(path)
        .toList()

    companion object {
        private val logger = KotlinLogging.logger {}
    }

}
