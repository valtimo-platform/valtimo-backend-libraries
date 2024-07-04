package com.ritense.formviewmodel.validation

import com.ritense.form.domain.FormIoFormDefinition
import com.ritense.formviewmodel.BaseTest
import org.junit.jupiter.api.Test
import java.util.Optional
import kotlin.test.assertTrue

class FormIOFormPropertiesExtractorTest : BaseTest() {

    @Test
    fun `should extract all field names for form`() {
        FormIOFormPropertiesExtractor.extractProperties(getValidFormDefinition().get().formDefinition).let {
            assertTrue(it.contains("test"))
            assertTrue(it.contains("age"))
            assertTrue(it.contains("dataContainer.nestedData"))
        }
    }

    private fun getValidFormDefinition(): Optional<FormIoFormDefinition> =
        Optional.of(formDefinitionOf("user-task-1"))
}
