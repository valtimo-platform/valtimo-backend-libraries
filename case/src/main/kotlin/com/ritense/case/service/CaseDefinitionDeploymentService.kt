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

package com.ritense.case.service

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.case_.repository.CaseDefinitionRepository
import com.ritense.importer.ValtimoImportService
import com.ritense.valtimo.changelog.service.ChangelogDeployer
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import mu.KotlinLogging
import org.apache.commons.lang3.StringUtils
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.ResourcePatternUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.FileNotFoundException


@Transactional
@Service
@SkipComponentScan
class CaseDefinitionDeploymentService(
    val resourceLoader: ResourceLoader,
    val valtimoImportService: ValtimoImportService,
    val caseDefinitionRepository: CaseDefinitionRepository,
    val changelogDeployer: ChangelogDeployer,
) {
    @Order(Ordered.LOWEST_PRECEDENCE)
    @EventListener(ApplicationReadyEvent::class)
    fun deployOnStartup() {
        val absoluteBasePathLength = try {
            ResourcePatternUtils
                .getResourcePatternResolver(resourceLoader)
                .getResource("classpath:/config/case/")
                .file
                .absolutePath
                .length
        } catch (ex: FileNotFoundException) {
            // No resources found, nothing to import
            logger.info { "No case definitions found. Continuing startup without importing." }
            return
        }

        val resources = ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(PATH)
            .groupBy {
                val relativePath = it.file.absolutePath.substring(
                    absoluteBasePathLength
                )

                relativePath.substring(0, StringUtils.ordinalIndexOf(relativePath, "/", 3))
            }
            .map { (key, files) ->
                key to (files.map {
                    it.file.absolutePath.substring(
                        absoluteBasePathLength
                    ).substring(key.length) to it
                })
            }
        resources.forEach { (_, files) ->
            runWithoutAuthorization {
                valtimoImportService.importCaseDefinition(files, caseDefinitionRepository.findAll().map { it.id })
            }
        }

        // Group by 1st * and 2nd *
        // Turn back into list of list resources
        // Import for each list of resources in the list
        // If one fails, everything fails.
        // TODO Implement triggering of imports

        changelogDeployer.deployAll()
    }

    //private fun getRelativeMap

    companion object {
        private const val PATH = "classpath:config/case/*/*/**/*.*" // TODO: Determine if we want to do config/case/ instead
        val logger = KotlinLogging.logger {}
    }
}
