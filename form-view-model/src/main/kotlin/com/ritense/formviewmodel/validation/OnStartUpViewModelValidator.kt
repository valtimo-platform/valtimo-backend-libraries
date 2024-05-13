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
import com.ritense.formviewmodel.event.FormViewModelSubmissionHandler
import com.ritense.formviewmodel.event.FormViewModelSubmissionHandlerFactory
import com.ritense.formviewmodel.viewmodel.Submission
import com.ritense.formviewmodel.viewmodel.ViewModelLoader
import mu.KotlinLogging
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

class OnStartUpViewModelValidator(
    private val formIoFormDefinitionService: FormIoFormDefinitionService,
    private val viewModelLoaders: List<ViewModelLoader<*>>,
    private val formViewModelSubmissionHandlerFactory: FormViewModelSubmissionHandlerFactory
) : ApplicationRunner {

    override fun run(args: ApplicationArguments?) {
        validate()
    }

    fun validate() {
        for (viewModelLoader in viewModelLoaders) {
            val formDefinition =
                formIoFormDefinitionService.getFormDefinitionByName(viewModelLoader.getFormName()).get()
            validateViewModel(viewModelLoader, formDefinition).let { missingProperties ->
                logger.error {
                    "The following properties are missing in the view model for form " +
                        "(${viewModelLoader.getFormName()}): $missingProperties"
                }
                // Validate submission for the view model
                formViewModelSubmissionHandlerFactory.getFormViewModelSubmissionHandler(
                    viewModelLoader.getFormName()
                )?.let {
                    validateSubmission(it, formDefinition).let { missingSubmissionProperties ->
                        logger.error {
                            "The following properties are missing in the submission for form " +
                                "(${viewModelLoader.getFormName()}): $missingSubmissionProperties"
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

    fun validateSubmission(
        submissionHandler: FormViewModelSubmissionHandler<out Submission>,
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
        val fieldNames = extractFieldNames(submission)
        return fieldNames.filter { fieldName ->
            fieldName !in formDefinition.inputFields.map { it["key"].asText() }
        }
    }

    fun extractFieldNames(kClass: KClass<*>, prefix: String = ""): List<String> {
        val results = mutableListOf<String>()

        // Iterate over each member property of the class
        for (prop in kClass.memberProperties) {
            prop.isAccessible = true  // Make private properties accessible

            // Determine the return type of the property
            val returnType = prop.returnType.classifier as? KClass<*>
            if (returnType != null && returnType.isData) {
                // If the return type is a data class, recurse into it
                results.addAll(extractFieldNames(returnType, "$prefix${prop.name}."))
            } else {
                // Otherwise, add the property name to results
                results.add("$prefix${prop.name}")
            }
        }
        return results
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}