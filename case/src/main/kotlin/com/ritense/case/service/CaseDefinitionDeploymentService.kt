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

package com.ritense.case.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.case.domain.CaseDefinitionSettings
import com.ritense.case.repository.CaseDefinitionSettingsRepository
import mu.KotlinLogging
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.event.EventListener
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.ResourcePatternUtils
import org.springframework.data.repository.findByIdOrNull
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.StreamUtils
import java.io.InputStream
import java.nio.charset.StandardCharsets

open class CaseDefinitionDeploymentService(
    private val resourceLoader: ResourceLoader,
    private val objectMapper: ObjectMapper,
    private val caseDefinitionSettingsRepository: CaseDefinitionSettingsRepository
) {

    @Transactional
    @EventListener(ApplicationStartedEvent::class)
    open fun deployCaseDefinitions() {
        logger.info { "Deploying case definitions" }
        loadCaseDefinitionResources().forEach { resource ->
            if (resource.filename != null) {
                deploy(resource)
            }
        }
    }

    private fun deploy(caseDefinition: Resource) {
        deploy(caseDefinition.filename!!.substringBeforeLast("."), caseDefinition.inputStream)
    }

    private fun deploy(caseDefinitionName: String, content: InputStream) {
        deploy(caseDefinitionName, StreamUtils.copyToString(content, StandardCharsets.UTF_8))
    }

    fun deploy(caseDefinitionName: String, configToLoad: String) {
        logger.debug("Deploying case definition {}", caseDefinitionName)
        val caseDefinitionSettings = caseDefinitionSettingsRepository.findByIdOrNull(caseDefinitionName)

        if (caseDefinitionSettings != null) {
            val updater = objectMapper.readerForUpdating(caseDefinitionSettings)
            val updatedCaseDefinitionSettings = updater.readValue<CaseDefinitionSettings>(configToLoad)
            caseDefinitionSettingsRepository.save(updatedCaseDefinitionSettings)
            logger.debug {"Case definition $caseDefinitionName was updated"}
        } else {
            throw RuntimeException("Attempted to update settings for case that does not exist $caseDefinitionName")
        }
    }

    private fun loadCaseDefinitionResources(): Array<Resource> {
        return ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(CASE_DEFINITIONS_PATH)
    }

    companion object {
        private const val CASE_DEFINITIONS_PATH = "classpath:config/case/definition/*.json"
        val logger = KotlinLogging.logger {}
    }
}
