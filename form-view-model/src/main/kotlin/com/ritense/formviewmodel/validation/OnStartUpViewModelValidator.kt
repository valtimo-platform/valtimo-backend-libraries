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

package com.ritense.formviewmodel.validation

import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.form.domain.FormIoFormDefinition
import com.ritense.form.service.impl.FormIoFormDefinitionService
import com.ritense.formviewmodel.submission.FormViewModelStartFormSubmissionHandler
import com.ritense.formviewmodel.submission.FormViewModelStartFormSubmissionHandlerFactory
import com.ritense.formviewmodel.submission.FormViewModelUserTaskSubmissionHandler
import com.ritense.formviewmodel.submission.FormViewModelUserTaskSubmissionHandlerFactory
import com.ritense.formviewmodel.viewmodel.FormViewModelLoader
import com.ritense.formviewmodel.viewmodel.Submission
import com.ritense.formviewmodel.viewmodel.ViewModelLoader
import mu.KotlinLogging
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import kotlin.reflect.KClass

class OnStartUpViewModelValidator(
    private val formIoFormDefinitionService: FormIoFormDefinitionService,
    private val viewModelLoaders: List<ViewModelLoader<*>>,
    private val formViewModelStartFormSubmissionHandlerFactory: FormViewModelStartFormSubmissionHandlerFactory,
    private val formViewModelUserTaskSubmissionHandlerFactory: FormViewModelUserTaskSubmissionHandlerFactory
) {

    @EventListener(ApplicationReadyEvent::class)
    fun validate() {
        for (viewModelLoader in viewModelLoaders.filterIsInstance(FormViewModelLoader::class.java)) {
            validateViewModelLoader(viewModelLoader)
        }
    }

    fun validateViewModelLoader(
        viewModelLoader: FormViewModelLoader<*>
    ) {
        val formDefinition =
            formIoFormDefinitionService.getFormDefinitionByName(viewModelLoader.getFormName())
                .orElseThrow{ NoSuchElementException("Could not find form [${viewModelLoader.getFormName()}] declared in ${viewModelLoader.javaClass}") }

        // Note: Forms added via console get a warning notice.
        if (!formDefinition.isReadOnly) {
            logger.warn {
                "This form (${viewModelLoader.getFormName()}) is not read-only. This means that the form definition is not added via configuration." +
                    "Be cautious when changing the form definition because this is only validated on ApplicationReadyEvent"
            }
        }
        validateViewModel(viewModelLoader, formDefinition).let { missingProperties ->
            if (missingProperties.isNotEmpty()) {
                logger.error {
                    "The following properties are missing in the view model for form " +
                        "(${viewModelLoader.getFormName()}): $missingProperties"
                }
                // Validate Start form submission for the view model
                formViewModelStartFormSubmissionHandlerFactory.getHandlerForFormValidation(
                    viewModelLoader.getFormName()
                )?.let {
                    validateStartFormSubmission(it, formDefinition).let { missingSubmissionProperties ->
                        if (missingSubmissionProperties.isNotEmpty()) {
                            logger.error {
                                "The following properties are missing in the start form submission for form " +
                                    "(${viewModelLoader.getFormName()}): $missingSubmissionProperties"
                            }
                        }
                    }
                }

                formViewModelUserTaskSubmissionHandlerFactory.getHandlerForFormValidation(
                    viewModelLoader.getFormName()
                )?.let {
                    validateUserTaskSubmission(it, formDefinition).let { missingSubmissionProperties ->
                        if (missingSubmissionProperties.isNotEmpty()) {
                            logger.error {
                                "The following properties are missing in the user task submission for form " +
                                    "(${viewModelLoader.getFormName()}): $missingSubmissionProperties"
                            }
                        }
                    }
                }
            }
        }
    }

    fun validateViewModel(
        viewModelLoader: ViewModelLoader<*>,
        formDefinition: FormIoFormDefinition
    ): List<String> {
        return getAllMissingProperties(viewModelLoader.getViewModelType(), formDefinition)
    }

    fun validateStartFormSubmission(
        submissionHandler: FormViewModelStartFormSubmissionHandler<out Submission>,
        formDefinition: FormIoFormDefinition
    ): List<String> {
        val submissionType = submissionHandler.getSubmissionType()::class
        if (submissionType.simpleName == ObjectNode::class.simpleName) {
            logger.error {
                "Submission type for form ${formDefinition.name} is ObjectNode. " +
                    "This is not advised. Please create a data class for the submission."
            }
        }
        return getAllMissingProperties(submissionHandler.getSubmissionType(), formDefinition)
    }

    fun validateUserTaskSubmission(
        submissionHandler: FormViewModelUserTaskSubmissionHandler<out Submission>,
        formDefinition: FormIoFormDefinition
    ): List<String> {
        val submissionType = submissionHandler.getSubmissionType()::class
        if (submissionType.simpleName == ObjectNode::class.simpleName) {
            logger.error {
                "Submission type for form ${formDefinition.name} is ObjectNode. " +
                    "This is not advised. Please create a data class for the submission."
            }
        }
        return getAllMissingProperties(submissionHandler.getSubmissionType(), formDefinition)
    }

    private fun getAllMissingProperties(
        submission: KClass<*>,
        formDefinition: FormIoFormDefinition
    ): List<String> {
        val fieldNames = DataClassPropertiesExtractor.extractProperties(submission)
        return fieldNames.filter { fieldName ->
            fieldName !in FormIOFormPropertiesExtractor.extractProperties(formDefinition.formDefinition)
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}