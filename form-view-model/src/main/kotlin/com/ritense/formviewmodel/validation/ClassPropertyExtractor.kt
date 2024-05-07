package com.ritense.formviewmodel.validation

import com.ritense.formviewmodel.viewmodel.ViewModel

class ClassPropertyExtractor {
    fun extractProperties(clazz: Class<ViewModel>): List<String> {
        return clazz.declaredFields.map { it.name }
    }
}