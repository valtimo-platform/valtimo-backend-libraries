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

import com.ritense.case.domain.CaseDefinition
import com.ritense.case.repository.CaseDefinitionSettingsRepository
import mu.KotlinLogging
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.event.EventListener
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.ResourcePatternUtils
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.StreamUtils
import java.io.InputStream
import java.nio.charset.StandardCharsets

open class CaseDefinitionDeploymentService(
    private val resourceLoader: ResourceLoader,
    private val caseDefinitionSettingsRepository: CaseDefinitionSettingsRepository
) {

    @Transactional
    @EventListener(ApplicationStartedEvent::class)
    open fun deployCaseDefinitions() {
        logger.info { "Deploying plugin categories" }
        try {
            loadCaseDefinitionResources().forEach { resource ->
                if (resource.filename != null) {
                    deploy(resource)
                }
            }
        } catch (e: Exception) {
            throw RuntimeException("TODO", e)
        }
    }

    private fun deploy(caseDefinition: Resource) {
        deploy(caseDefinition.filename!!.substringBeforeLast("."), caseDefinition.inputStream)
    }

    private fun deploy(caseDefinitionName: String, content: InputStream) {
        deploy(caseDefinitionName, StreamUtils.copyToString(content, StandardCharsets.UTF_8))
    }

    fun deploy(caseDefinitionName: String, caseDefinitionSettings: String) {
        val optionalCaseDefinition = caseDefinitionSettingsRepository.findById(caseDefinitionName)
        val caseDefinition = optionalCaseDefinition.orElse(
            CaseDefinition(caseDefinitionName)
        )
        try {
            val existingDefinition = formFlowService.findLatestDefinitionByKey(formFlowKey)
            var definitionId = FormFlowDefinitionId.newId(formFlowKey)

            if (existingDefinition != null) {
                if (formFlowDefinitionConfig.contentEquals(existingDefinition)) {
                    logger.info("Form Flow already deployed - {}", definitionId.toString())
                    return
                } else {
                    definitionId = FormFlowDefinitionId.nextVersion(existingDefinition.id)
                    logger.info("Form Flow changed. Deploying next version - {}", definitionId.toString())
                }
            }

            formFlowService.save(formFlowDefinitionConfig.toDefinition(definitionId))
            logger.info("Deployed Form Flow - {}", definitionId.toString())
        } catch (e: Exception) {
            throw RuntimeException("Failed to deploy Form Flow $formFlowKey", e)
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
