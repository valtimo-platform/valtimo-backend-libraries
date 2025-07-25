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

class ValueResolverPropertyKey {
    companion object {
        // CORE
        const val DOCUMENT_ID = "documentId"
        const val PROCESS_INSTANCE_ID = "processInstanceId"
        const val VARIABLE_SCOPE = "variableScope"

        // IKO
        const val IKO_DATA_AGGREGATE_KEY = "ikoDataAggregateKey"
        const val IKO_DATA_REQUEST_KEY = "ikoDataRequestKey"

        // Other
        const val ID = "id"
        const val PAGEABLE = "pageable"
        const val TAB_KEY = "tabKey"
        const val WIDGET_KEY = "widgetKey"

    }
}