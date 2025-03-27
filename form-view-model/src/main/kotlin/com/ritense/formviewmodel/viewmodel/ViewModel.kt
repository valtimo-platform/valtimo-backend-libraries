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
import com.ritense.valtimo.camunda.domain.CamundaTask

interface ViewModel {

    @Deprecated("Deprecated since 12.6.0", replaceWith = ReplaceWith("update(task, page, document)"))
    fun update(task: CamundaTask? = null): ViewModel {
        return this
    }

    @Deprecated("Deprecated since 12.6.0", replaceWith = ReplaceWith("update(task, page, document)"))
    fun update(
        task: CamundaTask? = null,
        page: Int?
    ): ViewModel {
        return update(task)
    }

    fun update(
        task: CamundaTask? = null,
        page: Int?,
        document: JsonSchemaDocument? = null
    ): ViewModel {
        return update(task, page)
    }

}