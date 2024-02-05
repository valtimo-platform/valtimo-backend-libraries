/*
 *  Copyright 2015-2023 Ritense BV, the Netherlands.
 *
 *  Licensed under EUPL, Version 1.2 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.ritense.valtimo.formflow.web.rest.result

import com.ritense.formflow.domain.definition.FormFlowDefinition

class FormFlowDefinitionResponse(
    val key: String,
    val versions: List<Long>
) {
    companion object {
        fun of(formFlowDefinitions: List<FormFlowDefinition>): FormFlowDefinitionResponse {
            val key = formFlowDefinitions[0].id.key
            assert(formFlowDefinitions.all { it.id.key == key })
            val versions = formFlowDefinitions.map { it.id.version }.sorted()
            return FormFlowDefinitionResponse(key = key, versions = versions)
        }
    }
}
