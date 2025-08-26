/*
 * Copyright 2015-2025 Ritense BV, the Netherlands.
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

package com.ritense.valtimo

import com.oracle.truffle.js.scriptengine.GraalJSScriptEngine
import org.camunda.bpm.engine.impl.scripting.engine.DefaultScriptEngineResolver
import org.graalvm.polyglot.Context
import org.graalvm.polyglot.HostAccess
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

class AllowedClassesScriptEngineResolver(
    scriptEngineManager: ScriptEngineManager,
    otherAllowedClasses: Set<String> = emptySet()
) : DefaultScriptEngineResolver(scriptEngineManager) {
    val ALL_ALLOWED = ALLOWED + otherAllowedClasses

    override fun getJavaScriptScriptEngine(language: String?): ScriptEngine {
        val ctx = Context.newBuilder("js")
            .allowHostAccess(HostAccess.ALL)
            .allowHostClassLookup(ALL_ALLOWED::contains)

        return GraalJSScriptEngine.create(null, ctx)
    }

    companion object {
        private val ALLOWED = mutableSetOf<String?>(
            "java.util.ArrayList",
            "org.joda.time.DateTime",
            "java.util.Date",
            "java.lang.Math",
            "org.camunda.spin.Spin",
            //java.time classes
            "java.time.Clock",
            "java.time.Duration",
            "java.time.Instant",
            "java.time.LocalDate",
            "java.time.LocalDateTime",
            "java.time.LocalTime",
            "java.time.MonthDay",
            "java.time.OffsetDateTime",
            "java.time.OffsetTime",
            "java.time.Period",
            "java.time.Year",
            "java.time.YearMonth",
            "java.time.ZonedDateTime",
            "java.time.ZoneId",
            "java.time.ZoneOffset",
        )
    }
}