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

package com.ritense.formviewmodel.submission

import com.ritense.formviewmodel.viewmodel.Submission
import org.springframework.transaction.annotation.Transactional
import kotlin.reflect.KClass
import kotlin.reflect.full.allSupertypes

@Transactional
interface FormViewModelStartFormSubmissionHandler<T : Submission> {

    /**
     * Determines whether this handler supports the specified form name.
     *
     * @param formName the name of the form to check
     * @return `true` if the form name is supported, `false` otherwise
     */
    fun supports(formName: String): Boolean

     /**
     * Handles the form submission process for starting a process.
     * In order to start a process please dispatch StartProcessCommand.
     *
     * Example code
     * ```
     * dispatchCommand(
     *     StartProcessCommand(
     *         caseInstanceId = yourCreatedCaseInstanceId,
     *         processDefinitionKey = yourProcessDefinitionKey,
     *         businessKey = businessKey,
     *         processVariables = mapOf()
     *     )
     * )
     * ```
     * @param documentDefinitionName the name of the document definition
     * @param processDefinitionKey the key of the process definition
     * @param submission the submission to be handled
     * @param <T> the type of the submission
     * @see com.ritense.formviewmodel.commandhandling.StartProcessCommand}`
     */
    fun <T> handle(
        documentDefinitionName: String,
        processDefinitionKey: String,
        submission: T
    )

    /**
     * Retrieves the type of the submission.
     *
     * This method uses reflection to determine the type of the submission.
     *
     * @return the class type of the submission
     * @throws IllegalArgumentException if the submission type cannot be resolved
     */
    @Suppress("UNCHECKED_CAST")
    fun getSubmissionType(): KClass<T> =
        this::class.allSupertypes.first { it.classifier == FormViewModelStartFormSubmissionHandler::class }.arguments.first().type?.let { it.classifier as KClass<T> }
            ?: throw IllegalArgumentException("Could not resolve SubmissionType for ${this::class}")

}