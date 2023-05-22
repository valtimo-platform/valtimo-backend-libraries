package com.ritense.authorization

import java.util.concurrent.Callable


class AuthorizationContext {

    companion object {
        private val ignoreAuthorizationThreadLocal = ThreadLocal.withInitial { false }

        @JvmStatic
        val ignoreAuthorization: Boolean
            get() = ignoreAuthorizationThreadLocal.get()

        @JvmStatic
        fun <T> runWithoutAuthorization(callable: Callable<T>): T {
            return try {
                ignoreAuthorizationThreadLocal.set(true)
                callable.call()
            } catch (e: RuntimeException) {
                throw e
            } catch (e: Exception) {
                throw RuntimeException(e)
            } finally {
                ignoreAuthorizationThreadLocal.set(false)
            }
        }
    }
}