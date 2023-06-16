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

import com.ritense.valtimo.changelog.domain.ChangesetDeployer
import mu.KotlinLogging
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.transaction.annotation.Transactional

@Transactional
class ChangelogDeployer(
    private val changelogService: ChangelogService,
    private val changesetDeployers: List<ChangesetDeployer>,
) {

    @EventListener(ApplicationReadyEvent::class)
    fun deployAll() {
        logger.info { "Running deployer" }

        changesetDeployers.forEach { it.before() }

        changesetDeployers
            .flatMap { changesetDeployer ->
                changelogService.loadResources(changesetDeployer.getPath()).map { Pair(changesetDeployer, it) }
            }
            .sortedBy { it.second.uri }
            .forEach { (changesetDeployer, resource) ->
                val filename = changelogService.getFilename(resource)
                logger.info { "Running deployer changeset: $filename" }
                val resourceContent = resource.inputStream.bufferedReader().use { it.readText() }
                deploy(changesetDeployer, filename, resourceContent)
            }
        logger.info { "Finished running deployer" }
    }

    fun deploy(changesetDeployer: ChangesetDeployer, filename: String, resourceContent: String) {
        changesetDeployer.getChangelogDetails(filename, resourceContent).forEach { changesetDetails ->
            val md5sum = changelogService.computeMd5sum(changesetDetails.valueToChecksum)
            if (changelogService.isNewValidChangeset(changesetDetails.changesetId, md5sum)) {
                changesetDetails.deploy()
                changelogService.saveChangeset(changesetDetails.changesetId, changesetDetails.key, filename, md5sum)
            }
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }

}
