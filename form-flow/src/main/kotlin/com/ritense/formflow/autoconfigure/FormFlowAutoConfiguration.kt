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

package com.ritense.formflow.autoconfigure

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.jsontype.NamedType
import com.ritense.formflow.domain.definition.configuration.step.FormStepTypeProperties
import com.ritense.formflow.handler.ApplicationReadyEventHandler
import com.ritense.formflow.handler.FormFlowStepTypeHandler
import com.ritense.formflow.repository.FormFlowAdditionalPropertiesSearchRepository
import com.ritense.formflow.repository.FormFlowDefinitionRepository
import com.ritense.formflow.repository.FormFlowInstanceRepository
import com.ritense.formflow.repository.FormFlowStepInstanceRepository
import com.ritense.formflow.repository.FormFlowStepRepository
import com.ritense.formflow.repository.MySqlFormFlowAdditionalPropertiesSearchRepository
import com.ritense.formflow.repository.PostgresFormFlowAdditionalPropertiesSearchRepository
import com.ritense.formflow.service.FormFlowDeploymentService
import com.ritense.formflow.service.FormFlowService
import com.ritense.formflow.service.ObjectMapperConfigurer
import javax.persistence.EntityManager
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ResourceLoader
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@EnableJpaRepositories(
    basePackageClasses = [
        FormFlowDefinitionRepository::class,
        FormFlowStepRepository::class,
        FormFlowInstanceRepository::class,
        FormFlowStepInstanceRepository::class]
)
@EntityScan(basePackages = ["com.ritense.formflow.domain"])
class FormFlowAutoConfiguration {

    @Bean
    fun formStepPropertiesType(): NamedType {
        return NamedType(FormStepTypeProperties::class.java, "form")
    }

    @Bean
    fun formFlowObjectMapper(
        objectMapper: ObjectMapper,
        stepPropertiesTypes: Collection<NamedType>
    ): ObjectMapperConfigurer {
        return ObjectMapperConfigurer(objectMapper, stepPropertiesTypes)
    }

    @Bean
    @ConditionalOnMissingBean(ApplicationReadyEventHandler::class)
    fun applicationReadyEventHandler(
        applicationContext: ApplicationContext
    ): ApplicationReadyEventHandler {
        return ApplicationReadyEventHandler(applicationContext)
    }

    @Bean
    @ConditionalOnMissingBean(FormFlowService::class)
    fun formFlowService(
        formFlowDefinitionRepository: FormFlowDefinitionRepository,
        formFlowInstanceRepository: FormFlowInstanceRepository,
        formFlowAdditionalPropertiesSearchRepository: FormFlowAdditionalPropertiesSearchRepository,
        formFlowStepTypeHandlers: List<FormFlowStepTypeHandler>
    ): FormFlowService {
        return FormFlowService(
            formFlowDefinitionRepository,
            formFlowInstanceRepository,
            formFlowAdditionalPropertiesSearchRepository,
            formFlowStepTypeHandlers
        )
    }

    @Bean
    @ConditionalOnProperty(prefix = "valtimo", name = ["database"], havingValue = "postgres")
    fun postgresFormFlowAdditionalPropertiesSearchRepository(
        entityManager: EntityManager,
        objectMapper: ObjectMapper
    ): FormFlowAdditionalPropertiesSearchRepository {
        return PostgresFormFlowAdditionalPropertiesSearchRepository(entityManager, objectMapper)
    }

    @Bean
    @ConditionalOnProperty(prefix = "valtimo", name = ["database"], havingValue = "mysql", matchIfMissing = true)
    fun mysqlFormFlowAdditionalPropertiesSearchRepository(
        entityManager: EntityManager
    ): FormFlowAdditionalPropertiesSearchRepository {
        return MySqlFormFlowAdditionalPropertiesSearchRepository(entityManager)
    }

    @Bean
    @ConditionalOnMissingBean(FormFlowDeploymentService::class)
    fun formFlowDeploymentService(
        resourceLoader: ResourceLoader,
        formFlowService: FormFlowService,
        objectMapper: ObjectMapper,
    ): FormFlowDeploymentService {
        return FormFlowDeploymentService(
            resourceLoader,
            formFlowService,
            objectMapper
        )
    }
}
