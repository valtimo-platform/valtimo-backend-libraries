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

package com.ritense.authorization.permission.condition

import com.ritense.authorization.BaseIntegrationTest
import com.ritense.authorization.permission.condition.PermissionConditionOperator.EQUAL_TO
import com.ritense.authorization.permission.condition.PermissionConditionOperator.GREATER_THAN
import com.ritense.authorization.permission.condition.PermissionConditionOperator.GREATER_THAN_OR_EQUAL_TO
import com.ritense.authorization.permission.condition.PermissionConditionOperator.IN
import com.ritense.authorization.permission.condition.PermissionConditionOperator.LESS_THAN
import com.ritense.authorization.permission.condition.PermissionConditionOperator.LESS_THAN_OR_EQUAL_TO
import com.ritense.authorization.permission.condition.PermissionConditionOperator.LIST_CONTAINS
import com.ritense.authorization.permission.condition.PermissionConditionOperator.NOT_EQUAL_TO
import com.ritense.authorization.testimpl.TestEntity
import com.ritense.authorization.testimpl.TestEntityRepository
import kotlin.test.Ignore
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

@Transactional
class PermissionConditionOperatorIntTest @Autowired constructor(
    val testEntityRepository: TestEntityRepository
) : BaseIntegrationTest() {

    lateinit var nullEntity: TestEntity
    lateinit var fourEntity: TestEntity
    lateinit var fiveEntity: TestEntity
    lateinit var sixEntity: TestEntity

    @BeforeEach
    fun setup() {
        this.nullEntity = testEntityRepository.save(TestEntity(fruits = mutableListOf(null, "apple"), someNumber = null))
        this.fourEntity = testEntityRepository.save(TestEntity(fruits = mutableListOf("apple", "banana"), someNumber = 4))
        this.fiveEntity = testEntityRepository.save(TestEntity(someNumber = 5))
        this.sixEntity = testEntityRepository.save(TestEntity(someNumber = 6))
    }

    @Test
    fun `NOT_EQUAL_TO should evaluate null value`() {
        val results = findByNumber(NOT_EQUAL_TO, null)

        Assertions.assertThat(results).contains(fourEntity, fiveEntity, sixEntity)
        Assertions.assertThat(results).doesNotContain(nullEntity)
    }

    @Test
    fun `NOT_EQUAL_TO should evaluate number value`() {
        val results = findByNumber(NOT_EQUAL_TO, 5)

        Assertions.assertThat(results).contains(nullEntity, fourEntity, sixEntity)
        Assertions.assertThat(results).doesNotContain(fiveEntity)
    }

    @Test
    fun `EQUAL_TO should evaluate null value`() {
        val results = findByNumber(EQUAL_TO, null)

        Assertions.assertThat(results).containsOnly(nullEntity)
    }

    @Test
    fun `EQUAL_TO should evaluate number value`() {
        val results = findByNumber(EQUAL_TO, 5)

        Assertions.assertThat(results).containsOnly(fiveEntity)
    }

    @Test
    fun `GREATER_THAN should evaluate number value`() {
        val results = findByNumber(GREATER_THAN, 5)

        Assertions.assertThat(results).containsOnly(sixEntity)
    }

    @Test
    fun `GREATER_THAN_OR_EQUAL_TO should evaluate number value`() {
        val results = findByNumber(GREATER_THAN_OR_EQUAL_TO, 5)

        Assertions.assertThat(results).contains(fiveEntity, sixEntity)
        Assertions.assertThat(results).doesNotContain(nullEntity, fourEntity)
    }

    @Test
    fun `LESS_THAN should evaluate number value`() {
        val results = findByNumber(LESS_THAN, 5)

        Assertions.assertThat(results).containsOnly(fourEntity)
    }

    @Test
    fun `LESS_THAN_OR_EQUAL_TO should evaluate number value`() {
        val results = findByNumber(LESS_THAN_OR_EQUAL_TO, 5)

        Assertions.assertThat(results).contains(fourEntity, fiveEntity)
        Assertions.assertThat(results).doesNotContain(nullEntity, sixEntity)
    }

    @Test
    @Ignore("This should match the evaluate() logic, but it does not")
    fun `CONTAINS should match null item`() {
        val results = findByFruits(LIST_CONTAINS, null)

        //TODO: fix this
        Assertions.assertThat(results).containsOnly(nullEntity)
    }

    @Test
    fun `CONTAINS should match multiple items`() {
        val results = findByFruits(LIST_CONTAINS, "apple")

        Assertions.assertThat(results).containsOnly(nullEntity, fourEntity)
    }

    @Test
    fun `CONTAINS should match single item`() {
        val results = findByFruits(LIST_CONTAINS, "banana")

        Assertions.assertThat(results).containsOnly(fourEntity)
    }

    @Test
    fun `IN should not match`() {
        val results = findMultipleByNumber(IN, listOf(1))

        Assertions.assertThat(results).isEmpty()
    }

    @Test
    fun `IN should match single value`() {
        val results = findMultipleByNumber(IN, listOf(4))

        Assertions.assertThat(results).containsOnly(fourEntity)
    }

    @Test
    fun `IN should match multiple values`() {
        val results = findMultipleByNumber(IN, listOf(1, 2, 4, 5))

        Assertions.assertThat(results).containsOnly(fourEntity, fiveEntity)
    }

    private fun findByFruits(op: PermissionConditionOperator, fruit: String?): MutableList<TestEntity> =
        testEntityRepository.findAll { root, _, criteriaBuilder ->
            op.toPredicate<Int>(
                criteriaBuilder,
                root.get<Int>("fruits"),
                fruit
            )
        }

    private fun findByNumber(op: PermissionConditionOperator, number: Int?): MutableList<TestEntity> =
        testEntityRepository.findAll { root, _, criteriaBuilder ->
            op.toPredicate<Int>(
                criteriaBuilder,
                root.get<Int>("someNumber"),
                number
            )
        }

    private fun findMultipleByNumber(op: PermissionConditionOperator, numbers: List<Int?>?): MutableList<TestEntity> =
        testEntityRepository.findAll { root, _, criteriaBuilder ->
            op.toPredicate<Int>(
                criteriaBuilder,
                root.get<Int>("someNumber"),
                numbers
            )
        }
}