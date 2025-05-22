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
import kotlin.reflect.KClass

val MDC_ERROR_CONTEXT: ThreadLocal<Pair<Throwable, Map<String, String>>> = ThreadLocal()

inline fun <T> withLoggingContext(
    contextKey: KClass<*>,
    contextValue: Any?,
    body: () -> T
): T = withLoggingContext(contextKey.java, contextValue.toString(), body)

inline fun <T> withLoggingContext(
    contextKey: Class<*>,
    contextValue: Any?,
    body: () -> T
): T = withLoggingContext(contextKey.canonicalName, contextValue.toString(), body)

fun withLoggingContext(
    contextKey: Class<*>,
    contextValue: String?,
    runnable: Runnable
) {
    return withLoggingContext(mapOf(contextKey.canonicalName to contextValue), true) {
        runnable.run()
    }
}

inline fun <T> withLoggingContext(
    contextKey: String,
    contextValue: String?,
    body: () -> T
): T = withLoggingContext(contextKey to contextValue, true, body)

inline fun <T> withLoggingContext(
    pair: Pair<String, String?>,
    body: () -> T
): T = withLoggingContext(pair, true, body)

inline fun <T> withLoggingContext(
    vararg pair: Pair<String, String?>,
    body: () -> T
): T = withLoggingContext(*pair, restorePrevious = true, body = body)

inline fun <T> withLoggingContext(
    map: Map<String, String?>,
    body: () -> T
): T = withLoggingContext(map, true, body)

inline fun <T> withLoggingContext(
    pair: Pair<String, String?>,
    restorePrevious: Boolean = true,
    body: () -> T
): T = mu.withLoggingContext(pair, restorePrevious) { catchMdcErrorContext(body) }

inline fun <T> withLoggingContext(
    vararg pair: Pair<String, String?>,
    restorePrevious: Boolean = true,
    body: () -> T
): T = mu.withLoggingContext(*pair, restorePrevious = restorePrevious) { catchMdcErrorContext(body) }

inline fun <T> withLoggingContext(
    map: Map<String, String?>,
    restorePrevious: Boolean = true,
    body: () -> T
): T = mu.withLoggingContext(map, restorePrevious) { catchMdcErrorContext(body) }

inline fun <T> withErrorLoggingContext(
    body: () -> T
): T {
    val mdcErrorContext = MDC_ERROR_CONTEXT.get()
    return if (mdcErrorContext == null) {
        body()
    } else {
        mu.withLoggingContext(mdcErrorContext.second, true, body)
    }
}

fun setErrorLoggingContext() {
    val mdcErrorContext = MDC_ERROR_CONTEXT.get()
    if (mdcErrorContext != null) {
        MDC.setContextMap(mdcErrorContext.second)
    }
}

inline fun <T> catchMdcErrorContext(body: () -> T): T {
    val mdcContext = MDC.getCopyOfContextMap()
    return try {
        body()
    } catch (e: Throwable) {
        var rootCause: Throwable = e
        while (rootCause.cause != null) {
            rootCause = rootCause.cause!!
        }
        val existing = MDC_ERROR_CONTEXT.get()
        if (existing?.first == rootCause) {
            MDC_ERROR_CONTEXT.set(rootCause to (mdcContext + existing.second))
        } else {
            MDC_ERROR_CONTEXT.set(rootCause to mdcContext)
        }
        throw e
    }
}