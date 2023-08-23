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

package com.ritense.externalevent.autoconfigure

import com.ritense.document.service.DocumentService
import com.ritense.document.service.impl.JsonSchemaDocumentService
import com.ritense.externalevent.config.MappedCasesConfig
import com.ritense.externalevent.messaging.ExternalDomainMessage
import com.ritense.externalevent.messaging.builder.CaseMessageSender
import com.ritense.externalevent.messaging.builder.TaskMessageSender
import com.ritense.externalevent.messaging.`in`.CompleteTaskMessage
import com.ritense.externalevent.messaging.`in`.CreateExternalCaseMessage
import com.ritense.externalevent.messaging.`in`.ExternalIdUpdatedConfirmationMessage
import com.ritense.externalevent.service.ExternalCaseService
import com.ritense.externalevent.service.ExternalTaskService
import com.ritense.form.service.impl.FormIoFormDefinitionService
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.tenancy.TenantResolver
import com.ritense.valtimo.contract.config.ValtimoProperties
import com.ritense.valtimo.contract.mail.MailSender
import mu.KotlinLogging
import org.camunda.bpm.engine.RuntimeService
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks
import java.util.function.Consumer
import java.util.function.Supplier

@Configuration
@EnableConfigurationProperties(value = [MappedCasesConfig::class])
class ExternalEventAutoConfiguration {

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    fun caseMessageSender(
        externalCaseService: ExternalCaseService,
        documentService: JsonSchemaDocumentService
    ): CaseMessageSender {
        return CaseMessageSender(
            externalCaseService = externalCaseService,
            documentService = documentService,
            execution = null
        )
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    fun taskMessageSender(
        externalTaskService: ExternalTaskService,
        documentService: JsonSchemaDocumentService,
        valtimoProperties: ValtimoProperties,
        mailSender: MailSender
    ): TaskMessageSender {
        return TaskMessageSender(
            externalTaskService = externalTaskService,
            documentService = documentService,
            delegateTask = null,
            valtimoProperties = valtimoProperties,
            mailSender = mailSender
        )
    }

    // Representing a queue/exchange for all external domain messages
    @Bean
    fun sink(): Sinks.Many<ExternalDomainMessage> {
        return Sinks.many().multicast().onBackpressureBuffer()
    }

    // Representing a consumer for CreateExternalCaseMessage
    @Bean
    fun createExternalCaseConsumer(externalCaseService: ExternalCaseService): Consumer<CreateExternalCaseMessage>? {
        return Consumer<CreateExternalCaseMessage> { message: CreateExternalCaseMessage ->
            externalCaseService.createExternalCase(message)
            logger.debug { "Received case id: ${message.caseId} with submission: ${message.submission}" }
        }
    }

    // Representing a consumer for CompleteTaskMessage
    @Bean
    fun completeTaskConsumer(externalTaskService: ExternalTaskService): Consumer<CompleteTaskMessage>? {
        return Consumer<CompleteTaskMessage> { m: CompleteTaskMessage ->
            externalTaskService.completeTask(m)
            logger.debug {
                "Received complete task message: case id" +
                    ": ${m.externalCaseId} task id: ${m.taskId} with submission: ${m.submission}"
            }
        }
    }

    // Representing a consumer for UpdatedExternalIdMessage
    @Bean
    fun updatedExternalIdConsumer(externalCaseService: ExternalCaseService): Consumer<ExternalIdUpdatedConfirmationMessage>? {
        return Consumer<ExternalIdUpdatedConfirmationMessage> { message: ExternalIdUpdatedConfirmationMessage ->
            externalCaseService.processExternalIdUpdateConfirmation(message)
            logger.debug { "Received update confirmation for case id: ${message.externalId} " }
        }
    }

    // Representing a publisher for all out-going external domain messages
    @Bean
    fun externalDomainMessageSupplier(sink: Sinks.Many<ExternalDomainMessage>): Supplier<Flux<Message<ExternalDomainMessage>?>> =
        Supplier {
            sink.asFlux().map { message ->
                MessageBuilder
                    .withPayload(message)
                    .setHeader("spring.cloud.stream.sendto.destination", message.destination)
                    .build()
            }
        }

    @Bean
    fun externalCaseService(
        documentService: DocumentService,
        processDocumentService: ProcessDocumentService,
        mappedCasesConfig: MappedCasesConfig,
        sink: Sinks.Many<ExternalDomainMessage>,
        runtimeService: RuntimeService,
        tenantResolver: TenantResolver
    ): ExternalCaseService {
        return ExternalCaseService(
            documentService,
            processDocumentService,
            mappedCasesConfig,
            sink,
            runtimeService,
            tenantResolver
        )
    }

    @Bean
    fun externalTaskService(
        documentService: JsonSchemaDocumentService,
        processDocumentService: ProcessDocumentService,
        formDefinitionService: FormIoFormDefinitionService,
        sink: Sinks.Many<ExternalDomainMessage>,
        tenantResolver: TenantResolver
    ): ExternalTaskService {
        return ExternalTaskService(
            documentService,
            processDocumentService,
            formDefinitionService,
            sink,
            tenantResolver
        )
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }

}
