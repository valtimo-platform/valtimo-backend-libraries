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

package com.ritense.iko.service

import com.ritense.iko.domain.IkoDataAggregateListColumn
import com.ritense.iko.domain.IkoDataAggregateListColumnId
import com.ritense.iko.repository.IkoDataAggregateListColumnRepository
import com.ritense.search.domain.SearchListColumn
import com.ritense.search.service.SearchListColumnService
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@SkipComponentScan
@Service
class IkoListColumnService(
    private val listColumnService: SearchListColumnService,
    private val ikoDataAggregateListColumnRepository: IkoDataAggregateListColumnRepository,
) {

    fun findByKey(ikoDataAggregateKey: String, columnKey: String): SearchListColumn? {
        return ikoDataAggregateListColumnRepository.findByIdIkoDataAggregateKeyAndColumnKey(
            ikoDataAggregateKey,
            columnKey
        )?.column
    }

    fun getByKey(ikoDataAggregateKey: String, columnKey: String): SearchListColumn {
        return findByKey(ikoDataAggregateKey, columnKey)
            ?: error("Unknown column key: $columnKey")
    }

    fun findAllColumnsByIkoDataAggregateKey(ikoDataAggregateKey: String): List<SearchListColumn> {
        return ikoDataAggregateListColumnRepository.findAllByIdIkoDataAggregateKeyOrderByColumnOrder(ikoDataAggregateKey)
            .map { it.column }
    }

    fun deleteByKey(ikoDataAggregateKey: String, columnKey: String) {
        ikoDataAggregateListColumnRepository.deleteByIdIkoDataAggregateKeyAndColumnKey(
            ikoDataAggregateKey,
            columnKey
        )
    }

    fun create(ikoDataAggregateKey: String, column: SearchListColumn): SearchListColumn {
        require(
            ikoDataAggregateListColumnRepository.findByIdIkoDataAggregateKeyAndColumnKey(
                ikoDataAggregateKey,
                column.key
            ) == null
        )
        val createdColumn = listColumnService.create(column)
        ikoDataAggregateListColumnRepository.save(
            IkoDataAggregateListColumn(
                id = IkoDataAggregateListColumnId(ikoDataAggregateKey, column.id),
                column = createdColumn,
            )
        )
        return createdColumn
    }

    fun update(ikoDataAggregateKey: String, column: SearchListColumn): SearchListColumn {
        val ikoDataAggregateColumn =
            ikoDataAggregateListColumnRepository.findByIdIkoDataAggregateKeyAndColumnKey(
                ikoDataAggregateKey,
                column.key
            )
        requireNotNull(ikoDataAggregateColumn)
        val updatedColumn =
            listColumnService.update(column.copy(id = ikoDataAggregateColumn.id.columnId))
        ikoDataAggregateListColumnRepository.save(
            IkoDataAggregateListColumn(
                id = IkoDataAggregateListColumnId(ikoDataAggregateKey, updatedColumn.id),
                column = updatedColumn,
            )
        )
        return updatedColumn
    }

}