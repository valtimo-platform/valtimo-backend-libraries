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

package com.ritense.formviewmodel.viewmodel

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.form.domain.FormProcessLink
import com.ritense.form.service.FormDefinitionService
import com.ritense.processlink.domain.ProcessLink

class ViewModelLoaderFactory(
    private val viewModelLoaders: List<ViewModelLoader<*>>,
    private val formDefinitionService: FormDefinitionService
) {

    fun getViewModelLoader(processLink: ProcessLink): ViewModelLoader<out ViewModel>? {
        val formName = (processLink as? FormProcessLink)?.let {
            runWithoutAuthorization { formDefinitionService.getFormDefinitionById(processLink.formDefinitionId) }
                .orElse(null)?.name
        }

        return viewModelLoaders.find { loader ->
            loader.supports(processLink) || (formName?.let { (loader as? FormViewModelLoader)?.supports(formName) } ?: false)
        }
    }
}