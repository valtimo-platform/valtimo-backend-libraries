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

package com.ritense.valtimo.logging

import com.ritense.valtimo.contract.authentication.UserManagementService
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor
import org.camunda.bpm.engine.impl.jobexecutor.NotifyAcquisitionRejectedJobsHandler
import org.camunda.bpm.spring.boot.starter.CamundaBpmAutoConfiguration
import org.camunda.bpm.spring.boot.starter.configuration.impl.DefaultJobConfiguration.JobConfiguration
import org.camunda.bpm.spring.boot.starter.property.CamundaBpmProperties
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.core.task.TaskExecutor
import java.util.Optional

@AutoConfiguration
@AutoConfigureBefore(CamundaBpmAutoConfiguration::class)
class CamundaLoggingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(JobExecutor::class)
    @ConditionalOnProperty(
        prefix = "camunda.bpm.job-execution",
        name = ["enabled"],
        havingValue = "true",
        matchIfMissing = true
    )
    fun jobExecutor(
        @Qualifier(JobConfiguration.CAMUNDA_TASK_EXECUTOR_QUALIFIER) taskExecutor: TaskExecutor?,
        properties: CamundaBpmProperties
    ): JobExecutor {
        val springJobExecutor = LoggingSpringJobExecutor()
        springJobExecutor.taskExecutor = taskExecutor
        springJobExecutor.rejectedJobsHandler = NotifyAcquisitionRejectedJobsHandler()

        val jobExecution = properties.getJobExecution();
        Optional.ofNullable(jobExecution.lockTimeInMillis).ifPresent(springJobExecutor::setLockTimeInMillis);
        Optional.ofNullable(jobExecution.maxJobsPerAcquisition).ifPresent(springJobExecutor::setMaxJobsPerAcquisition);
        Optional.ofNullable(jobExecution.waitTimeInMillis).ifPresent(springJobExecutor::setWaitTimeInMillis);
        Optional.ofNullable(jobExecution.maxWait).ifPresent(springJobExecutor::setMaxWait);
        Optional.ofNullable(jobExecution.backoffTimeInMillis).ifPresent(springJobExecutor::setBackoffTimeInMillis);
        Optional.ofNullable(jobExecution.maxBackoff).ifPresent(springJobExecutor::setMaxBackoff);
        Optional.ofNullable(jobExecution.backoffDecreaseThreshold).ifPresent(springJobExecutor::setBackoffDecreaseThreshold);
        Optional.ofNullable(jobExecution.waitIncreaseFactor).ifPresent(springJobExecutor::setWaitIncreaseFactor);

        return springJobExecutor
    }

    @Bean
    @ConditionalOnMissingBean(UserLoggingFilter::class)
    fun userLoggingFilter(userManagementService: UserManagementService) = UserLoggingFilter(userManagementService)

}