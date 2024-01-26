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

package com.ritense.valtimo.script

import com.oracle.truffle.js.scriptengine.GraalJSScriptEngine
import org.camunda.bpm.engine.ScriptEvaluationException
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.VariableScope
import org.camunda.bpm.engine.impl.scripting.ExecutableScript
import org.camunda.bpm.engine.impl.scripting.ScriptFactory
import javax.script.Bindings
import javax.script.CompiledScript
import javax.script.ScriptEngine
import javax.script.ScriptException

data class ValtimoScriptFactory(
    private val valtimoScriptRepository: ValtimoScriptRepository
) : ScriptFactory() {

    override fun createScriptFromResource(language: String, resource: String): ExecutableScript {
        assert(language == "javascript")
        val script = valtimoScriptRepository.findById(resource).orElseThrow()
        val compiledScript = GraalJSScriptEngine.create().compile(prefillValtimoScript(script.content))
        return CompiledExecutableScript(language, compiledScript)
    }

    override fun createScriptFromSource(language: String, source: String): ExecutableScript {
        return super.createScriptFromSource(language, prefillValtimoScript(source))
    }

    private fun prefillValtimoScript(javascript: String): String {
        var result = javascript
        val matcher = "[^(@@@@)]*\\{\\{([a-zA-Z0-9-_]+)}}[^(@@@@)]*".toRegex().matchEntire(javascript)
        val scriptKeys = matcher?.groups
            ?.filterIndexed { i, _ -> i > 0 }
            ?.filterNotNull()
            ?.map { it.value }
            ?: emptyList()
        scriptKeys.forEach { key ->
            val script = valtimoScriptRepository.findById(key).orElseThrow()
            result = result.replace("\\{\\{$key}}".toRegex(), script.content)
        }
        return result
    }
}

class CompiledExecutableScript(
    language: String,
    private val compiledScript: CompiledScript
) : ExecutableScript(language) {
    public override fun evaluate(scriptEngine: ScriptEngine, variableScope: VariableScope, bindings: Bindings): Any {
        return try {
            compiledScript.eval(bindings)
        } catch (e: ScriptException) {
            if (e.cause is BpmnError) {
                throw (e.cause as BpmnError?)!!
            }
            val activityIdMessage = getActivityIdExceptionMessage(variableScope)
            throw ScriptEvaluationException("Unable to evaluate script" + activityIdMessage + ": " + e.message, e)
        }
    }
}