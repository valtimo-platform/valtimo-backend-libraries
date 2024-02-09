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

package com.ritense.authorization.criteriabuilder

import com.ritense.authorization.testimpl.TestEntity
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Root
import org.junit.jupiter.api.Test
import org.mockito.Mockito.anyList
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class AbstractQueryWrapperTest {
    @Test
    fun `should only add a single root for 'from'`() {
        val clazz = TestEntity::class.java

        val query : CriteriaQuery<TestEntity> = mock()
        val queryWrapper = AbstractQueryWrapper<TestEntity>(query)

        val root : Root<TestEntity> = mock()
        whenever(root.javaType).thenReturn(clazz)
        whenever(query.from(clazz)).thenReturn(root)
        whenever(query.roots).thenReturn(setOf())
            .thenReturn(setOf(root))

        queryWrapper.from(clazz)
        queryWrapper.from(clazz)

        verify(query, times(1)).from(clazz)
    }

    @Test
    fun `should only add a single groupBy`() {
        val root : Root<TestEntity> = mock()

        val query : CriteriaQuery<TestEntity> = mock()
        whenever(query.groupBy(anyList())).thenReturn(query)
        val queryWrapper = AbstractQueryWrapper<TestEntity>(query)

        queryWrapper.groupBy(root, root)

        verify(query, times(1)).groupBy(listOf(root))
    }
}