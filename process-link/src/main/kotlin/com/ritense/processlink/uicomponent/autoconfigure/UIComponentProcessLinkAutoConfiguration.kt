/*
 * Copyright 2015-2025 Ritense BV, the Netherlands.
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

package com.ritense.processlink.uicomponent.autoconfigure

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.processlink.uicomponent.mapper.UIComponentProcessLinkMapper
import com.ritense.processlink.uicomponent.mapper.UIComponentProcessLinkModule
import com.ritense.processlink.uicomponent.service.UIComponentProcessLinkActivityHandler
import com.ritense.processlink.uicomponent.service.UIComponentSupportedProcessLinksHandler
import com.ritense.valtimo.camunda.domain.CamundaTask
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order

@Configuration
@EntityScan("com.ritense.processlink.uicomponent.domain")
class UIComponentProcessLinkAutoConfiguration {


    @Bean
    @ConditionalOnMissingBean(UIComponentProcessLinkModule::class)
    fun uiComponentProcessLinkModule() = UIComponentProcessLinkModule()

    @Bean
    @ConditionalOnMissingBean(UIComponentProcessLinkMapper::class)
    fun uiComponentProcessLinkMapper(
        objectMapper: ObjectMapper
    ) = UIComponentProcessLinkMapper()

    @Bean
    @Order(50)
    @ConditionalOnMissingBean(UIComponentSupportedProcessLinksHandler::class)
    fun uiComponentSupportedProcessLinksHandler() = UIComponentSupportedProcessLinksHandler()

    @Bean
    @ConditionalOnMissingBean(UIComponentProcessLinkActivityHandler::class)
    @ConditionalOnClass(CamundaTask::class) // This bean cannot be instantiated when :core is excluded (see :plugin)
    fun uiComponentProcessLinkActivityHandler() = UIComponentProcessLinkActivityHandler()
}