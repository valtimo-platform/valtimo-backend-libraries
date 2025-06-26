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

package com.ritense.iko.repository

import com.ritense.iko.domain.IkoConnectorConfig
import org.springframework.data.jpa.domain.Specification

class IkoConnectorConfigSpecificationHelper {

    companion object {

        const val KEY: String = "key"
        const val TITLE: String = "title"
        const val TYPE: String = "type"

        @JvmStatic
        fun query() = Specification<IkoConnectorConfig> { _, _, cb ->
            cb.conjunction()
        }

        @JvmStatic
        fun byKey(key: String) = Specification<IkoConnectorConfig> { root, _, cb ->
            cb.equal(root.get<String>(KEY), key)
        }

        @JvmStatic
        fun byTitleContains(titlePart: String) = Specification<IkoConnectorConfig> { root, _, cb ->
            cb.like(root[TITLE], "%$titlePart%")
        }

        @JvmStatic
        fun byType(type: String) = Specification<IkoConnectorConfig> { root, _, cb ->
            cb.equal(root.get<String>(TYPE), type)
        }
    }
}