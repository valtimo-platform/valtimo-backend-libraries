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

package com.ritense.valtimo.dashboard

import com.ritense.valtimo.operaton.repository.OperatonTaskRepository
import com.ritense.valtimo.operaton.repository.OperatonTaskSpecificationHelper.Companion.all
import com.ritense.valtimo.contract.dashboard.WidgetDataSource
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Expression
import jakarta.persistence.criteria.Path
import jakarta.persistence.criteria.Root

class TaskWidgetDataSource(
    private val taskRepository: OperatonTaskRepository
) {
    @WidgetDataSource("task-count", "Task count")
    fun getTaskCount(taskCountDataSourceProperties: TaskCountDataSourceProperties): TaskCountDataResult {
        val taskSpec = all()
        val spec = taskSpec.and { root, _, criteriaBuilder ->
            criteriaBuilder.and(
                *taskCountDataSourceProperties.queryConditions?.map {
                    it.toPredicate(root, criteriaBuilder, this::getPathExpression)
                }?.toTypedArray() ?: arrayOf()
            )
        }

        val count = taskRepository.count(spec)
        val total = taskRepository.count(taskSpec)
        return TaskCountDataResult(count, total)
    }

    private fun <T> getPathExpression(
        valueClass: Class<T>,
        path: String,
        root: Root<*>,
        criteriaBuilder: CriteriaBuilder
    ): Expression<T> {
        var expr = root as Path<*>
        path.substringAfter(TASK_PREFIX).split('.').forEach {
            expr = expr.get<Any>(it)
        }

        return expr.`as`(valueClass);
    }

    companion object {
        private const val TASK_PREFIX = "task:"
    }
}