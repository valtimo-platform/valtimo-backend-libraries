package com.ritense.formlink.domain.impl.submission

import com.ritense.form.domain.FormIoFormDefinition
import org.apache.commons.io.IOUtils
import java.nio.charset.StandardCharsets
import java.util.Objects
import java.util.UUID

abstract class BaseTest {

    protected open fun rawFormDefinition(formDefinitionId: String): String {
        return IOUtils.toString(
            Objects.requireNonNull(Thread.currentThread().contextClassLoader.getResourceAsStream("config/form/$formDefinitionId.json")),
            StandardCharsets.UTF_8
        )
    }

    protected open fun formDefinitionOf(formDefinitionId: String): FormIoFormDefinition {
        val formDefinition: String = rawFormDefinition(formDefinitionId)
        return FormIoFormDefinition(UUID.randomUUID(), formDefinitionId, formDefinition, false)
    }
}