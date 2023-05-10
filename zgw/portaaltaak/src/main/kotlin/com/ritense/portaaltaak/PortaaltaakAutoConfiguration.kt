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

package com.ritense.portaaltaak

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.document.domain.impl.Mapper
import com.ritense.document.service.DocumentService
import com.ritense.objectmanagement.service.ObjectManagementService
import com.ritense.plugin.service.PluginService
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.valtimo.contract.config.LiquibaseMasterChangeLogLocation
import com.ritense.valtimo.service.CamundaProcessService
import com.ritense.valueresolver.ValueResolverService
import com.ritense.zakenapi.link.ZaakInstanceLinkService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.TaskService
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order

@Configuration
class PortaaltaakAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(PortaaltaakPluginFactory::class)
    fun portaaltaakPluginFactory(
        pluginService: PluginService,
        objectManagementService: ObjectManagementService,
        valueResolverService: ValueResolverService,
        processDocumentService: ProcessDocumentService,
        zaakInstanceLinkService: ZaakInstanceLinkService,
        taskService: TaskService
    ): PortaaltaakPluginFactory {
        return PortaaltaakPluginFactory(
            pluginService,
            objectManagementService,
            valueResolverService,
            processDocumentService,
            zaakInstanceLinkService,
            taskService
        )
    }

    @Bean
    @ConditionalOnMissingBean(PortaalTaakEventListener::class)
    fun portaalTaakEventListener(
        pluginService: PluginService,
        objectManagementService: ObjectManagementService,
        processDocumentService: ProcessDocumentService,
        processService: CamundaProcessService,
        taskService: TaskService,
        documentService: DocumentService,
        runtimeService: RuntimeService,
        valueResolverService: ValueResolverService,
        objectMapper: ObjectMapper
    ): PortaalTaakEventListener {
        return PortaalTaakEventListener(
            objectManagementService,
            pluginService,
            processDocumentService,
            processService,
            taskService,
            runtimeService,
            valueResolverService,
            objectMapper
        )
    }

    @Order(Ordered.HIGHEST_PRECEDENCE + 20)
    @Bean
    @ConditionalOnMissingBean(name = ["portaaltaakLiquibaseMasterChangeLogLocation"])
    fun portaaltaakLiquibaseMasterChangeLogLocation(): LiquibaseMasterChangeLogLocation {
        return LiquibaseMasterChangeLogLocation("config/liquibase/portaaltaak-master.xml")
    }
}
