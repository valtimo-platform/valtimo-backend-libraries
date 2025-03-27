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

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.form.domain.FormProcessLink
import com.ritense.form.service.FormDefinitionService
import com.ritense.formviewmodel.viewmodel.Submission
import com.ritense.processlink.domain.ProcessLink

class FormViewModelStartFormSubmissionHandlerFactory(
    private val handlers: List<FormViewModelStartFormSubmissionHandler<*>>,
    private val formDefinitionService: FormDefinitionService
) {

    @Deprecated("Deprecated since 12.6.0", replaceWith = ReplaceWith("getHandlerForFormValidation(formName)"))
    fun getHandler(formName: String) = getHandlerForFormValidation(formName)

    fun getHandlerForFormValidation(formName: String): FormViewModelStartFormSubmissionHandler<out Submission>? {
        return handlers.find { it.supports(formName) }
    }

    fun getHandler(processLink: ProcessLink): FormViewModelStartFormSubmissionHandler<out Submission>? {
        val formName = (processLink as? FormProcessLink)?.let { runWithoutAuthorization {
            formDefinitionService.getFormDefinitionById(processLink.formDefinitionId)
        }.orElse(null)?.name }

        return handlers.find { handler ->
            handler.supports(processLink) ||
                (formName?.let { handler.supports(formName) } ?: false)
        }
    }

}