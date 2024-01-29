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

package com.ritense.script.service

import com.ritense.valtimo.contract.script.ScriptPrefiller
import org.springframework.transaction.annotation.Transactional

open class ValtimoScriptPrefiller(
    private val scriptService: ScriptService,
) : ScriptPrefiller {

    @Transactional
    override fun prefillScript(scriptWithPlaceholders: String): String {
        var result = scriptWithPlaceholders
        "\\{\\{([a-zA-Z0-9-_]+)}}".toRegex().findAll(scriptWithPlaceholders)
            .filter { it.groupValues.size == 2 }
            .map { it.groupValues[0] to it.groupValues[1] }
            .distinct()
            .forEach { (placeholder, key) ->
                val script = scriptService.getScript(key)
                result = result.replace(placeholder, script.content)
            }
        return result
    }
}
