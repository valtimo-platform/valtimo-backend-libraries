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

package com.ritense.valtimo.importchangelog

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.valtimo.contract.importchangelog.ChangesetDeployer
import com.ritense.valtimo.importchangelog.domain.ImportChangeset
import com.ritense.valtimo.importchangelog.repository.ImportChangesetRepository
import liquibase.util.MD5Util
import liquibase.util.StringUtil
import mu.KotlinLogging
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.ResourcePatternUtils
import org.springframework.transaction.annotation.Transactional
import java.io.IOException
import java.text.Normalizer
import java.time.Instant
import kotlin.jvm.optionals.getOrNull

@Transactional
class ImportChangelogDeploymentService(
    private val resourceLoader: ResourceLoader,
    private val importChangesetRepository: ImportChangesetRepository,
    private val objectMapper: ObjectMapper,
    private val changesetDeployers: List<ChangesetDeployer>,
) {

    @EventListener(ApplicationReadyEvent::class)
    fun deployAll() {
        logger.info { "Running import deployer" }
        changesetDeployers.forEach { changesetDeployer ->
            loadResources(changesetDeployer.getPath()).forEach { resource ->
                val filename = getFilename(resource)
                logger.info { "Running import deployer changelog: $filename" }
                val resourceContent = resource.inputStream.bufferedReader().use { it.readText() }
                deploy(changesetDeployer, resourceContent, filename)
            }
        }
        logger.info { "Finished running import deployer" }
    }

    fun deploy(changesetDeployer: ChangesetDeployer, content: String, filename: String) {
        val changesetDetails = changesetDeployer.getChangesetDetails(filename, content)
        val md5sum = computeMd5sum(objectMapper.writeValueAsString(changesetDetails.valueToChecksum))
        val existing = importChangesetRepository.findById(changesetDetails.changesetId).getOrNull()

        if (existing != null) {
            if (md5sum != existing.md5sum) {
                throw RuntimeException("Computed checksum '$md5sum' doesn't match existing '${existing.md5sum}' for import $filename")
            } else {
                logger.debug { "Verified checksum '$md5sum' for import $filename" }
            }
        } else {
            changesetDeployer.deploy(content)
            saveChangeset(changesetDetails.changesetId, filename, md5sum)
        }
    }

    private fun getFilename(resource: Resource) = resource.uri.toASCIIString().substringAfterLast("resources/")

    private fun saveChangeset(changesetId: String, filename: String, md5sum: String) {
        importChangesetRepository.save(
            ImportChangeset(
                id = changesetId,
                filename = filename,
                dateExecuted = Instant.now(),
                orderExecuted = importChangesetRepository.getNextOrderExecuted() ?: 0,
                md5sum = md5sum
            )
        )
    }

    private fun computeMd5sum(valueToChecksum: String): String {
        return MD5Util.computeMD5(
            Normalizer.normalize(
                StringUtil.standardizeLineEndings(valueToChecksum)
                    .replace("\uFFFD", ""), Normalizer.Form.NFC
            )
        )
    }

    private fun getTypeFromFileName(filename: String?): String? {
        return if (filename == null) {
            null
        } else {
            Regex(".+\\.(.+)\\.json").find(filename)?.groupValues?.get(1)
        }
    }

    @Throws(IOException::class)
    private fun loadResources(path: String) = ResourcePatternUtils.getResourcePatternResolver(resourceLoader)
        .getResources(path)

    companion object {
        private val logger = KotlinLogging.logger {}
    }

}
