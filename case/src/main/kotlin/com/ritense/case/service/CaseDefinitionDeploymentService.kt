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

package com.ritense.case.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.readValue
import com.ritense.case.domain.CaseDefinitionSettings
import com.ritense.case.repository.CaseDefinitionSettingsRepository
import com.ritense.document.domain.event.DocumentDefinitionDeployedEvent
import java.nio.charset.StandardCharsets
import mu.KotlinLogging
import org.springframework.context.event.EventListener
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.ResourcePatternUtils
import org.springframework.data.repository.findByIdOrNull
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.StreamUtils

open class CaseDefinitionDeploymentService(
    private val resourceLoader: ResourceLoader,
    private val objectMapper: ObjectMapper,
    private val caseDefinitionSettingsRepository: CaseDefinitionSettingsRepository
) {

    @Transactional
    @EventListener(DocumentDefinitionDeployedEvent::class)
    open fun conditionalCreateCase(event: DocumentDefinitionDeployedEvent) {
        val documentDefinitionName = event.documentDefinition().id().name()
        val caseDefinitionSettings = caseDefinitionSettingsRepository.findByIdOrNull(documentDefinitionName)
        if (caseDefinitionSettings == null) {
            val resource = ResourcePatternUtils.getResourcePatternResolver(resourceLoader)
                .getResource("classpath:config/case/definition/$documentDefinitionName.json")

            if (resource.exists()) {
                deploy(
                    resource.filename!!.substringBeforeLast("."),
                    StreamUtils.copyToString(resource.inputStream, StandardCharsets.UTF_8)
                )
            } else {
                caseDefinitionSettingsRepository.save(CaseDefinitionSettings(documentDefinitionName))
            }
        }
    }

    private fun deploy(caseDefinitionName: String, configToLoad: String) {
        logger.debug { "Deploying case definition $caseDefinitionName" }
        val caseDefinitionSettings = caseDefinitionSettingsRepository.findByIdOrNull(caseDefinitionName)

        if (caseDefinitionSettings == null) {
            val settingsToDeploy = objectMapper.readValue<ObjectNode>(configToLoad)
                .put("name", caseDefinitionName)
            val createdCaseDefinitionSettings: CaseDefinitionSettings = objectMapper.convertValue(settingsToDeploy)

            caseDefinitionSettingsRepository.save(createdCaseDefinitionSettings)
            logger.debug { "Case definition $caseDefinitionName was created" }
        } else {
            logger.debug { "Attempted to update settings for case that already exist $caseDefinitionName" }
        }
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}
