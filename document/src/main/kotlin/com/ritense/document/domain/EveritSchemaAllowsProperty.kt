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
import org.everit.json.schema.ReferenceSchema
import org.everit.json.schema.Schema
import org.everit.json.schema.ValidationException
import org.everit.json.schema.regexp.Regexp

/** This method is a copy from org.everit.json.schema.Schema.definesProperty(.) but returns true when the schema allows additionalProperties */
fun Schema.allowsProperty(field: String): Boolean {
    return when (this) {
        is ObjectSchema -> allowsProperty(field)
        is ArraySchema -> allowsProperty(field)
        is CombinedSchema -> allowsProperty(field)
        is ReferenceSchema -> allowsProperty(field)
        else -> false
    }
}

/** Copied from ObjectSchema.definesProperty(.) but returns true when the schema allows additionalProperties */
private fun ObjectSchema.allowsProperty(field: String): Boolean {
    val headAndTail: Array<String?> = headAndTailOfJsonPointerFragment(field)
    val nextToken = headAndTail[0]!!
    val remaining = headAndTail[1]
    val field2 = headAndTail[2]!!
    return field2.isNotEmpty() && (allowsSchemaProperty(nextToken, remaining)
            || allowsPatternProperty(nextToken, remaining)
            || allowsSchemaDependencyProperty(field2)
            || permitsAdditionalProperties()) // <- This is the only line that is different from all definesProperty(.) implementations
}

/** Copied from ObjectSchema.definesSchemaProperty(.) but returns true when the schema allows additionalProperties */
private fun ObjectSchema.allowsSchemaProperty(current: String, remaining: String?): Boolean {
    val currentUnescaped = jsonPointerUnescape(current)
    return if (propertySchemas.containsKey(currentUnescaped)) {
        if (remaining != null) {
            propertySchemas[currentUnescaped]!!.allowsProperty(remaining)
        } else {
            true
        }
    } else false
}

/** Copied from ObjectSchema.definesPatternProperty(.) but returns true when the schema allows additionalProperties */
private fun ObjectSchema.allowsPatternProperty(current: String, remaining: String?): Boolean {
    val patternProperties: Map<Regexp, Schema> = getPrivateField("patternProperties")
    patternProperties.entries.forEach { (pattern, value) ->
        if (!pattern.patternMatchingFailure(current).isPresent
            && (remaining == null || value.allowsProperty(remaining))
        ) {
            return true
        }
    }
    return false
}

/** Copied from ObjectSchema.definesSchemaDependencyProperty(.) but returns true when the schema allows additionalProperties */
private fun ObjectSchema.allowsSchemaDependencyProperty(field: String): Boolean {
    if (schemaDependencies.containsKey(field)) {
        return true
    }
    for (schema in schemaDependencies.values) {
        if (schema.allowsProperty(field)) {
            return true
        }
    }
    return false
}

/** Copied from ArraySchema.definesProperty(.) but returns true when the schema allows additionalProperties */
private fun ArraySchema.allowsProperty(field: String): Boolean {
    val headAndTail: Array<String?> = headAndTailOfJsonPointerFragment(field)
    val nextToken = headAndTail[0]!!
    val remaining = headAndTail[1]
    val hasRemaining = remaining != null
    return try {
        tryPropertyDefinitionByNumericIndex(nextToken, remaining, hasRemaining)
    } catch (e: NumberFormatException) {
        tryPropertyDefinitionByMetaIndex(nextToken, remaining, hasRemaining)
    }
}


/** Copied from ArraySchema.tryPropertyDefinitionByMetaIndex(.) but returns true when the schema allows additionalProperties */
private fun ArraySchema.tryPropertyDefinitionByMetaIndex(
    nextToken: String,
    remaining: String?,
    hasRemaining: Boolean
): Boolean {
    val isAll = "all" == nextToken
    val isAny = "any" == nextToken
    if (!hasRemaining && (isAll || isAny)) {
        return true
    }
    if (isAll) {
        return if (allItemSchema != null) {
            allItemSchema.allowsProperty(remaining!!)
        } else {
            val allItemSchemasDefine: Boolean = itemSchemas.stream()
                .map { schema -> schema.allowsProperty(remaining!!) }
                .reduce(true) { a, b -> java.lang.Boolean.logicalAnd(a, b) }
            if (allItemSchemasDefine) {
                return if (schemaOfAdditionalItems != null) {
                    schemaOfAdditionalItems.allowsProperty(remaining!!)
                } else {
                    true
                }
            }
            false
        }
    } else if (isAny) {
        return if (allItemSchema != null) {
            allItemSchema.allowsProperty(remaining!!)
        } else {
            val anyItemSchemasDefine: Boolean = itemSchemas.stream()
                .map { schema -> schema.allowsProperty(remaining!!) }
                .reduce(false) { a, b -> java.lang.Boolean.logicalOr(a, b) }
            anyItemSchemasDefine || schemaOfAdditionalItems == null || schemaOfAdditionalItems.allowsProperty(remaining!!)
        }
    }
    return false
}

/** Copied from ArraySchema.tryPropertyDefinitionByNumericIndex(.) but returns true when the schema allows additionalProperties */
private fun ArraySchema.tryPropertyDefinitionByNumericIndex(
    nextToken: String,
    remaining: String?,
    hasRemaining: Boolean
): Boolean {
    val index = nextToken.toInt()
    if (index < 0) {
        return false
    }
    if (maxItems != null && maxItems <= index) {
        return false
    }
    return if (allItemSchema != null && hasRemaining) {
        allItemSchema.allowsProperty(remaining!!)
    } else {
        if (hasRemaining) {
            if (index < itemSchemas.size) {
                return itemSchemas[index].allowsProperty(remaining!!)
            }
            if (schemaOfAdditionalItems != null) {
                return schemaOfAdditionalItems.allowsProperty(remaining!!)
            }
        }
        getPrivateField("additionalItems")
    }
}

/** Copied from CombinedSchema.definesProperty(.) but returns true when the schema allows additionalProperties */
private fun CombinedSchema.allowsProperty(field: String): Boolean {
    val matching: MutableList<Schema> = ArrayList()
    for (subschema in subschemas) {
        if (subschema.allowsProperty(field)) {
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

/** Copied from ReferenceSchema.definesProperty(.) but returns true when the schema allows additionalProperties */
private fun ReferenceSchema.allowsProperty(field: String): Boolean {
    checkNotNull(referredSchema) { "referredSchema must be injected before validation" }
    return referredSchema.allowsProperty(field)
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

