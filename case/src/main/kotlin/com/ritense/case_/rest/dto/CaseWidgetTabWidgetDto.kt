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

package com.ritense.case_.rest.dto

import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
abstract class CaseWidgetTabWidgetDto(
    val key: String,
    val title: String,
    val width: Int,
    val highContrast: Boolean
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CaseWidgetTabWidgetDto) return false

        if (key != other.key) return false
        if (title != other.title) return false
        if (width != other.width) return false
        if (highContrast != other.highContrast) return false

        return true
    }

    override fun hashCode(): Int {
        var result = key.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + width
        result = 31 * result + highContrast.hashCode()
        return result
    }

    override fun toString(): String {
        return "CaseWidgetTabWidgetDto(key='$key', title='$title', width=$width, highContrast=$highContrast)"
    }
}
