/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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
import org.assertj.core.api.Assertions.assertThat
import org.hibernate.metamodel.model.domain.internal.EntityTypeImpl
import org.hibernate.query.criteria.internal.CriteriaBuilderImpl
import org.hibernate.query.criteria.internal.CriteriaQueryImpl
import org.hibernate.query.criteria.internal.path.RootImpl
import org.junit.jupiter.api.Test
import org.mockito.Mockito.RETURNS_DEEP_STUBS
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class AbstractQueryWrapperTest {

    private val testEntityType = EntityTypeImpl<TestEntity>(TestEntity::class.java, null, mock(), mock())

    @Test
    fun `should only add a single root for 'from'`() {
        val criteriaBuilder = mock<CriteriaBuilderImpl>(defaultAnswer = RETURNS_DEEP_STUBS)
        whenever(criteriaBuilder.entityManagerFactory.metamodel.entity(TestEntity::class.java))
            .thenReturn(testEntityType)
        val query = CriteriaQueryImpl(criteriaBuilder, TestEntity::class.java)
        val queryWrapper = AbstractQueryWrapper(query)

        queryWrapper.from(TestEntity::class.java)
        queryWrapper.from(TestEntity::class.java)

        assertThat(query.roots).hasSize(1)
        assertThat(query.roots.first().javaType).isEqualTo(TestEntity::class.java)
    }

    @Test
    fun `should only add a single groupBy`() {
        val query = CriteriaQueryImpl(mock(), TestEntity::class.java)
        val queryWrapper = AbstractQueryWrapper(query)
        val root = RootImpl(mock(), testEntityType)

        queryWrapper.groupBy(root, root)

        assertThat(query.groupList).hasSize(1)
        assertThat(query.groupList.first()).isEqualTo(root)
    }

}