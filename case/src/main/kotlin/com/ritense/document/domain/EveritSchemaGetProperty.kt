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

package com.ritense.document.domain

import com.fasterxml.jackson.core.type.TypeReference
import org.everit.json.schema.ArraySchema
import org.everit.json.schema.BooleanSchema
import org.everit.json.schema.CombinedSchema
import org.everit.json.schema.ConditionalSchema
import org.everit.json.schema.ConstSchema
import org.everit.json.schema.EmptySchema
import org.everit.json.schema.EnumSchema
import org.everit.json.schema.FalseSchema
import org.everit.json.schema.JSONPointer
import org.everit.json.schema.NotSchema
import org.everit.json.schema.NullSchema
import org.everit.json.schema.NumberSchema
import org.everit.json.schema.ObjectSchema
import org.everit.json.schema.ReferenceSchema
import org.everit.json.schema.Schema
import org.everit.json.schema.StringSchema
import org.everit.json.schema.TrueSchema
import org.everit.json.schema.regexp.Regexp

/** This method is similar to org.everit.json.schema.Schema.definesProperty(.)  */
fun Schema.getProperty(field: String): Schema? {
    return when (this) {
        is ObjectSchema -> getProperty(field)
        is ArraySchema -> getProperty(field)
        is CombinedSchema -> getProperty(field)
        is ReferenceSchema -> getProperty(field)
        else -> null
    }
}

private fun ObjectSchema.getProperty(field: String): Schema? {
    val headAndTail: Array<String?> = headAndTailOfJsonPointerFragment(field)
    val nextToken = headAndTail[0]!!
    val remaining = headAndTail[1]
    val field2 = headAndTail[2]!!
    return if (field2.isEmpty()) {
        null
    } else {
        getSchemaProperty(nextToken, remaining)
            ?: getPatternProperty(nextToken, remaining)
            ?: getSchemaDependencyProperty(field2)
    }
}

private fun ObjectSchema.getSchemaProperty(current: String, remaining: String?): Schema? {
    val currentUnescaped = jsonPointerUnescape(current)
    return if (propertySchemas.containsKey(currentUnescaped)) {
        if (remaining != null) {
            propertySchemas[currentUnescaped]!!.getProperty(remaining)
        } else {
            propertySchemas[currentUnescaped]!!
        }
    } else null
}

private fun ObjectSchema.getPatternProperty(current: String, remaining: String?): Schema? {
    val patternProperties = getPrivateField<Map<Regexp, Schema>>("patternProperties")
    patternProperties.entries.forEach { (pattern, value) ->
        if (!pattern.patternMatchingFailure(current).isPresent) {
            return if (remaining == null) {
                value
            } else {
                value.getProperty(remaining)
            }
        }
    }
    return null
}

private fun ObjectSchema.getSchemaDependencyProperty(field: String): Schema? {
    for (schema in schemaDependencies.values) {
        val property = schema.getProperty(field)
        if (property != null) {
            return property
        }
    }
    return null
}

private fun ArraySchema.getProperty(field: String): Schema? {
    val headAndTail: Array<String?> = headAndTailOfJsonPointerFragment(field)
    val nextToken = headAndTail[0]!!
    val remaining = headAndTail[1]
    val hasRemaining = remaining != null
    return try {
        tryGetPropertyDefinitionByNumericIndex(nextToken, remaining, hasRemaining)
    } catch (e: NumberFormatException) {
        tryGetPropertyDefinitionByMetaIndex(nextToken, remaining, hasRemaining)
    }
}


private fun ArraySchema.tryGetPropertyDefinitionByMetaIndex(
    nextToken: String,
    remaining: String?,
    hasRemaining: Boolean
): Schema? {
    val isAll = "all" == nextToken
    val isAny = "any" == nextToken
    if (!hasRemaining && (isAll || isAny)) {
        return this
    }
    if (isAll) {
        return if (allItemSchema != null) {
            allItemSchema.getProperty(remaining!!)
        } else {
            val allItemSchemasDefine: Boolean = itemSchemas.stream()
                .map { schema -> schema.definesProperty(remaining!!) }
                .reduce(true) { a, b -> java.lang.Boolean.logicalAnd(a, b) }
            if (allItemSchemasDefine) {
                return if (schemaOfAdditionalItems != null) {
                    schemaOfAdditionalItems.getProperty(remaining!!)
                } else {
                    this
                }
            }
            null
        }
    } else if (isAny) {
        return if (allItemSchema != null) {
            allItemSchema.getProperty(remaining!!)
        } else {
            val anyItemSchemasDefine: Boolean = itemSchemas.stream()
                .map { schema -> schema.definesProperty(remaining!!) }
                .reduce(false) { a, b -> java.lang.Boolean.logicalOr(a, b) }
            if (anyItemSchemasDefine) {
                return if (schemaOfAdditionalItems != null) {
                    schemaOfAdditionalItems.getProperty(remaining!!)
                } else {
                    this
                }
            }
            null
        }
    }
    return null
}

private fun ArraySchema.tryGetPropertyDefinitionByNumericIndex(
    nextToken: String,
    remaining: String?,
    hasRemaining: Boolean
): Schema? {
    val index = nextToken.toInt()
    if (index < 0) {
        return null
    }
    if (maxItems != null && maxItems <= index) {
        return null
    }
    return if (allItemSchema != null && hasRemaining) {
        allItemSchema.getProperty(remaining!!)
    } else {
        if (hasRemaining) {
            if (index < itemSchemas.size) {
                return itemSchemas[index].getProperty(remaining!!)
            }
            if (schemaOfAdditionalItems != null) {
                return schemaOfAdditionalItems.getProperty(remaining!!)
            }
        }
        null
    }
}

private fun CombinedSchema.getProperty(field: String): Schema? {
    for (subschema in subschemas) {
        if (subschema.definesProperty(field)) {
            return subschema.getProperty(field)
        }
    }
    return null
}

private fun ReferenceSchema.getProperty(field: String): Schema? {
    checkNotNull(referredSchema) { "referredSchema must be injected before validation" }
    return referredSchema.getProperty(field)
}

private fun Schema.headAndTailOfJsonPointerFragment(field: String): Array<String?> {
    val method = Schema::class.java.declaredMethods.single { it.name == "headAndTailOfJsonPointerFragment" }
    method.isAccessible = true
    return method.invoke(this, field) as Array<String?>
}

private fun <T> Any.getPrivateField(fieldName: String): T {
    val field = javaClass.getDeclaredField(fieldName)
    field.isAccessible = true
    return field.get(this) as T
}

private fun jsonPointerUnescape(token: String): String {
    val method = JSONPointer::class.java.declaredMethods.single { it.name == "unescape" }
    method.isAccessible = true
    return method.invoke(null, token) as String
}

fun Schema.getTypeReference(): TypeReference<*> {
    return when (this) {
        is ArraySchema -> object : TypeReference<List<Any>>() {}
        is BooleanSchema -> object : TypeReference<Boolean>() {}
        is CombinedSchema -> object : TypeReference<Any>() {}
        is ConditionalSchema -> object : TypeReference<Any>() {}
        is ConstSchema -> object : TypeReference<Any>() {}
        is EnumSchema -> object : TypeReference<Any>() {}
        is FalseSchema -> object : TypeReference<Boolean>() {}
        is NotSchema -> object : TypeReference<Any>() {}
        is NullSchema -> object : TypeReference<Void>() {}
        is NumberSchema -> object : TypeReference<Number>() {}
        is ObjectSchema -> object : TypeReference<Map<String, Any>>() {}
        is ReferenceSchema -> object : TypeReference<Any>() {}
        is StringSchema -> object : TypeReference<String>() {}
        is TrueSchema -> object : TypeReference<Boolean>() {}
        is EmptySchema -> object : TypeReference<Void>() {}
        else -> object : TypeReference<Any>() {}
    }
}
