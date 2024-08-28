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

package com.ritense.logging

import org.slf4j.MDC

class ResourceLoggerContext {
    companion object {
        private const val MDC_VARIABLE_NAME = "relatedResources"

        private val relatedResources: ThreadLocal<MutableList<Pair<Class<*>, String>>> =
            ThreadLocal.withInitial { mutableListOf() }

        fun withResource(clazz: Class<*>, key: String, callback: () -> Unit) {
            try {
                relatedResources.set(relatedResources.get().apply {
                    this.add(Pair(clazz, key))
                })
                callback.invoke()
            } finally {
                relatedResources.set(relatedResources.get().apply {
                    this.removeLast()
                })
            }
        }

        fun putMDCResources() {
            MDC.put(MDC_VARIABLE_NAME, relatedResources.get().toString())
        }

        fun removeMDCResources() {
            MDC.remove(MDC_VARIABLE_NAME)
        }
    }
}