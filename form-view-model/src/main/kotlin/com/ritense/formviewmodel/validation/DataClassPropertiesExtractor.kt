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