package com.ritense.formviewmodel.domain.validation

import com.ritense.formviewmodel.domain.ViewModel

class ClassPropertyExtractor {
    fun extractProperties(clazz: Class<ViewModel>): List<String> {
        return clazz.declaredFields.map { it.name }
    }
}