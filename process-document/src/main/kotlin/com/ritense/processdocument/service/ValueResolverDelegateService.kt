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

package com.ritense.processdocument.service

import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valueresolver.ValueResolverService
import org.operaton.bpm.engine.delegate.DelegateExecution
import org.springframework.stereotype.Service

@Service
@SkipComponentScan
class ValueResolverDelegateService(
    private val valueResolverService: ValueResolverService,
) {

    fun resolveValue(execution: DelegateExecution, key: String): Any? {
        return valueResolverService.resolveValues(execution.processInstanceId, execution, listOf(key))[key]
    }

    fun handleValue(execution: DelegateExecution, key: String, value: Any?) {
        valueResolverService.handleValues(execution.processInstanceId, execution, mapOf(key to value))
    }

}