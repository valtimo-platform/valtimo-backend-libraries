package com.ritense.formviewmodel.domain.validation

import com.ritense.form.domain.FormIoFormDefinition


class FormDefinitionPrefixedKeysExtractor {
    fun getPrefixedKeys(form: FormIoFormDefinition): List<String> {
        return form.inputFields.filter {
            it["key"].asText().contains(PREFIX)
        }.map {
            it["key"].asText().removePrefix(PREFIX)
        }
    }

    companion object {
        const val PREFIX = "vm:"
    }
}