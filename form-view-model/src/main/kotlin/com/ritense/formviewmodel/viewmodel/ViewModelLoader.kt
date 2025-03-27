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

import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.processlink.domain.ProcessLink
import com.ritense.valtimo.camunda.domain.CamundaTask
import org.springframework.transaction.annotation.Transactional
import kotlin.reflect.KClass
import kotlin.reflect.full.allSupertypes

@Transactional
interface ViewModelLoader<T : ViewModel> {

    fun load(task: CamundaTask? = null, document: JsonSchemaDocument? = null): T = load(task)

    @Deprecated("Since 12.6", ReplaceWith("load(task, documentId)"))
    fun load(task: CamundaTask? = null): T = load(task, null)

    fun supports(processLink: ProcessLink): Boolean

    @Suppress("UNCHECKED_CAST")
    fun getViewModelType(): KClass<T> =
        this::class.allSupertypes.first { it.classifier == ViewModelLoader::class }.arguments.first().type?.let { it.classifier as KClass<T> }
            ?: throw IllegalArgumentException("Could not resolve ViewModelType for ${this::class}")
}