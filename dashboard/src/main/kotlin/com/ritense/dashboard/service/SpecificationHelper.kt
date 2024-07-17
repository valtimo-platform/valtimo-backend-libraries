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

package com.ritense.dashboard.service

import com.ritense.dashboard.domain.Dashboard
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Root
import org.springframework.data.jpa.domain.Specification
import java.util.*

class SpecificationHelper {
    companion object {
        fun orderByOrder(spec: Specification<Dashboard> ): Specification<Dashboard> {
            return Specification { root, query, builder ->
                query.orderBy(builder.asc(root.get<Int>("order")))
                return@Specification spec.toPredicate(root, query, builder);
            }
        }
    }
}