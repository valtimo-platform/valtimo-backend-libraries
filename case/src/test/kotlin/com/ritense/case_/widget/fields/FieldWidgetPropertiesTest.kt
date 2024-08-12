package com.ritense.case_.widget.fields

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