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

package com.ritense.valueresolver

import com.ritense.valtimo.contract.case_.CaseDefinitionId
import com.ritense.valueresolver.exception.ValueResolverValidationException
import org.camunda.bpm.engine.delegate.VariableScope
import java.util.function.Function

class TestCaseValueResolver(
) : ValueResolverFactory {

    override fun supportedPrefix(): String {
        return "testCase"
    }

    override fun createResolver(
        processInstanceId: String,
        variableScope: VariableScope
    ): Function<String, Any?> {
        return Function { "test" }
    }

    override fun createResolver(documentId: String): Function<String, Any?> {
        return Function { "test" }
    }

    override fun createValidator(documentDefinitionName: String): Function<String, Unit> {
        return Function { requestedValue ->
            if (!COLUMN_LIST.contains(requestedValue)) {
                throw ValueResolverValidationException("Unknown document column with name: $requestedValue")
            }
        }
    }

    override fun handleValues(processInstanceId: String, variableScope: VariableScope?, values: Map<String, Any?>) {
        val firstValue = values.iterator().next()
        throw NotImplementedError("Unable to handle value: {${firstValue.key} to ${firstValue.value}}")
    }

    override fun getResolvableKeyOptions(documentDefinitionName: String, caseDefinitionId: CaseDefinitionId): List<ValueResolverOption> {
        return createFieldList(COLUMN_LIST)
    }

    override fun getResolvableKeyOptions(documentDefinitionName: String): List<ValueResolverOption> {
        return createFieldList(COLUMN_LIST) +
            ValueResolverOption(
                "test",
                ValueResolverOptionType.COLLECTION,
                listOf(
                    ValueResolverOption("1", ValueResolverOptionType.FIELD),
                    ValueResolverOption("2", ValueResolverOptionType.FIELD),
                    ValueResolverOption("3", ValueResolverOptionType.FIELD),
                )
            )
    }

    companion object {
        val COLUMN_LIST = listOf(
            "4",
            "5",
            "6",
        )
    }
}
