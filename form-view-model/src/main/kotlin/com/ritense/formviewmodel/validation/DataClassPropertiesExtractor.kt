package com.ritense.formviewmodel.validation

import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

object DataClassPropertiesExtractor {
    fun extractProperties(kClass: KClass<*>, prefix: String = ""): List<String> {
        val results = mutableListOf<String>()

        // Iterate over each member property of the class
        for (prop in kClass.memberProperties) {
            prop.isAccessible = true  // Make private properties accessible

            // Determine the return type of the property
            val returnType = prop.returnType.classifier as? KClass<*>
            if (returnType != null && returnType.isData) {
                // If the return type is a data class, recurse into it
                results.addAll(extractProperties(returnType, "$prefix${prop.name}."))
            } else {
                // Otherwise, add the property name to results
                results.add("$prefix${prop.name}")
            }
        }
        return results
    }
}