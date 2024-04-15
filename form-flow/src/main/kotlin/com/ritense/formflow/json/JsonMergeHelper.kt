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

package com.ritense.formflow.json

import org.json.JSONArray
import org.json.JSONObject

object JsonMergeHelper {

    fun merge(source: JSONObject, target: JSONObject, mergeStrategy: String) {
        val keys = JSONObject.getNames(source) ?: arrayOf()
        for (key in keys) {
            val value = source[key]
            if (target.has(key) && value is JSONObject && target[key] is JSONObject) {
                merge(value, target.getJSONObject(key), mergeStrategy)
            } else if (target.has(key) && value is JSONArray && target[key] is JSONArray) {
                when (mergeStrategy) {
                    "override" -> target.put(key, value)
                    "deepmerge" -> deepMergeArray(value, target[key] as JSONArray)
                    else -> error("Unknown merge strategy $mergeStrategy")
                }
            } else {
                target.put(key, value)
            }
        }
    }

    private fun deepMergeArray(sourceArray: JSONArray, targetArray: JSONArray) {
        val sourceArrayIndexes = targetArray.mapIndexed { i, targetItem ->
            findIndexOfSimilarItemInArray(i, targetItem, sourceArray)
        }
        val newTargetArrayItems = sourceArrayIndexes.mapIndexed { i, sourceIndex ->
            if (sourceIndex == null || sourceArrayIndexes.subList(0, i).contains(sourceIndex)) {
                targetArray[i]
            } else {
                val sourceItem = sourceArray[sourceIndex]
                val targetItem = targetArray[i]
                if (sourceItem is JSONObject && targetItem is JSONObject) {
                    merge(targetItem, sourceItem, "deepmerge")
                    sourceItem
                } else if (sourceItem is JSONArray && targetItem is JSONArray) {
                    deepMergeArray(sourceItem, targetItem)
                } else {
                    sourceItem
                }
            }
        }

        targetArray.clear()
        newTargetArrayItems.forEach { targetArray.put(it) }
    }

    private fun findIndexOfSimilarItemInArray(startIndex: Int, item: Any, array: JSONArray): Int? {
        return ((startIndex..<array.length()) + (0..<startIndex))
            .firstOrNull { i -> isSimilar(item, array[i]) }
    }

    private fun isSimilar(source: Any?, target: Any?): Boolean {
        return if (source is JSONObject && target is JSONObject) {
            val allKeys = source.keySet() + target.keySet()
            allKeys.all { key -> !source.has(key) || !target.has(key) || isSimilar(source[key], target[key]) }
        } else if (source is JSONArray && target is JSONArray) {
            true
        } else {
            source == target
        }
    }
}