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

package com.ritense.widget.fields

import com.ritense.valtimo.contract.validation.ValidatorHolder.Companion.validate
import jakarta.validation.ConstraintViolationException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class FieldWidgetPropertiesTest {

    @Test
    fun `should not pass validation with empty columns list`() {
        val properties = FieldsWidgetProperties(
            listOf()
        )
        val ex = assertThrows<ConstraintViolationException> {
            validate(properties)
        }
        assertThat(ex.message).isEqualTo("columns: must not be empty")
    }

    @Test
    fun `should not pass validation with empty columns`() {
        val properties = FieldsWidgetProperties(
            listOf(listOf())
        )
        val ex = assertThrows<ConstraintViolationException> {
            validate(properties)
        }
        assertThat(ex.message).isEqualTo("columns[0].<list element>: must not be empty")
    }

    @Test
    fun `should not pass validation with when fields are empty`() {
        val properties = FieldsWidgetProperties(
            listOf(listOf(
                FieldsWidgetProperties.Field(key = "", title = "", value = "")
            ))
        )
        val ex = assertThrows<ConstraintViolationException> {
            validate(properties)
        }
        assertThat(ex.constraintViolations).hasSize(2)
        assertThat(ex.constraintViolations.single { it.propertyPath.toString() == "columns[0].<list element>[0].key" }.message).isEqualTo("must not be blank")
        assertThat(ex.constraintViolations.single { it.propertyPath.toString() == "columns[0].<list element>[0].value" }.message).isEqualTo("must not be blank")
    }
}