/*
 * Copyright 2015-2025 Ritense BV, the Netherlands.
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

package com.ritense.gzac.fvm

import com.ritense.formviewmodel.viewmodel.Submission
import com.ritense.formviewmodel.viewmodel.ViewModel
import com.ritense.valtimo.camunda.domain.CamundaTask
import mu.KLogger
import mu.KotlinLogging

class NoopViewModel : ViewModel, Submission {

    override fun update(task: CamundaTask?, page: Int?): ViewModel {
        logger.info { "Update called on ${this::class.java.simpleName}, taskId=${task?.id}, page=$page" }

        return this
    }

    companion object {
        private val logger: KLogger = KotlinLogging.logger {}
    }
}