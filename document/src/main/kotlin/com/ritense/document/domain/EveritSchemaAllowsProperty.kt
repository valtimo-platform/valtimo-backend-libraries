/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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

package com.ritense.document.domain

import org.everit.json.schema.ArraySchema
import org.everit.json.schema.CombinedSchema
import org.everit.json.schema.JSONPointer
import org.everit.json.schema.ObjectSchema
import org.everit.json.schema.Schema
import org.everit.json.schema.ValidationException
import org.everit.json.schema.regexp.Regexp
import kotlin.reflect.KClass

fun Schema.allowsProperty(field: String) = false

fun ObjectSchema.allowsProperty(field: String): Boolean {
    val headAndTail: Array<String?> = callPrivateMethod("headAndTailOfJsonPointerFragment", field)
    val nextToken = headAndTail[0]!!
    val remaining = headAndTail[1]
    val field2 = headAndTail[2]!!
    return field2.isNotEmpty() && (allowsSchemaProperty(nextToken, remaining)
            || callPrivateMethod("definesPatternProperty", nextToken, remaining)
            || callPrivateMethod("definesSchemaDependencyProperty", field2)
            || permitsAdditionalProperties())
}

private fun ObjectSchema.allowsSchemaProperty(current: String, remaining: String?): Boolean {
    var current: String? = current
    current = callPrivateStaticMethod(JSONPointer::class, "unescape", current)
    val hasSuffix = remaining != null
    return if (propertySchemas.containsKey(current)) {
        if (hasSuffix) {
            propertySchemas[current]!!.allowsProperty(remaining!!)
        } else {
            true
        }
    } else false
}

private fun ObjectSchema.definesPatternProperty(current: String, remaining: String?): Boolean {
    for ((pattern, value): Map.Entry<Regexp, Schema> in getRegexpPatternProperties().entries) {
        if (!pattern.patternMatchingFailure(current).isPresent) {
            if (remaining == null || value.definesProperty(remaining)) {
                return true
            }
        }
    }
    return false
}

private fun ObjectSchema.definesSchemaDependencyProperty(field: String): Boolean {
    if (schemaDependencies.containsKey(field)) {
        return true
    }
    for (schema in schemaDependencies.values) {
        if (schema.definesProperty(field)) {
            return true
        }
    }
    return false
}

fun ArraySchema.allowsProperty(field: String): Boolean {
    val headAndTail: Array<String?> = callPrivateMethod("headAndTailOfJsonPointerFragment", field)
    val nextToken = headAndTail[0]!!
    val remaining = headAndTail[1]
    val hasRemaining = remaining != null
    return try {
        callPrivateMethod("tryPropertyDefinitionByNumericIndex", nextToken, remaining, hasRemaining)
    } catch (e: NumberFormatException) {
        callPrivateMethod("tryPropertyDefinitionByNumericIndex", nextToken, remaining, hasRemaining)
    }
}

fun CombinedSchema.allowsProperty(field: String?): Boolean {
    val matching: MutableList<Schema> = ArrayList()
    for (subschema in subschemas) {
        if (subschema.definesProperty(field)) {
            matching.add(subschema)
        }
    }
    try {
        criterion.validate(subschemas.size, matching.size)
    } catch (e: ValidationException) {
        return false
    }
    return true
}

internal fun <T> Any.callPrivateMethod(methodName: String, vararg args: Any?): T {
    val method = javaClass.getDeclaredMethod(methodName)
    method.isAccessible = true
    return method.invoke(this, args) as T
}

internal fun <T, U : Any> callPrivateStaticMethod(cls: KClass<U>, methodName: String, vararg args: Any?): T {
    val method = cls.java.getDeclaredMethod(methodName)
    method.isAccessible = true
    return method.invoke(null, args) as T
}

