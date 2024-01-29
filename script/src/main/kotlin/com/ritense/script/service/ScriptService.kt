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

import com.ritense.script.domain.Script
import com.ritense.script.repository.ScriptRepository
import org.springframework.transaction.annotation.Transactional

@Transactional
class ScriptService(
    private val scriptRepository: ScriptRepository,
) {

    fun getAllScripts(): List<Script> {
        return scriptRepository.findAll()
    }

    fun getScript(scriptKey: String): Script {
        return scriptRepository.findById(scriptKey).orElseThrow()
    }

    fun createScript(script: Script): Script {
        assert(!scriptRepository.existsById(script.key)) { "Script with key '${script.key}' already exists" }
        return scriptRepository.save(script)
    }

    fun saveScript(script: Script): Script {
        return scriptRepository.save(script)
    }

    fun deleteScripts(scriptKeys: List<String>) {
        scriptKeys.forEach { deleteScript(it) }
    }

    fun deleteScript(scriptKey: String) {
        scriptRepository.deleteById(scriptKey)
    }
}
