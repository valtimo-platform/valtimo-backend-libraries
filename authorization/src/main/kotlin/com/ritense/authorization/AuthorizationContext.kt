package com.ritense.authorization

import java.util.concurrent.Callable


class AuthorizationContext {

    companion object {
        private val bypassAuthorizationThreadLocal = ThreadLocal.withInitial { false }

        @JvmStatic
        val bypassAuthorization: Boolean
            get() = bypassAuthorizationThreadLocal.get()

        @JvmStatic
        fun <T> runWithoutAuthorization(callable: Callable<T>): T {
            return try {
                bypassAuthorizationThreadLocal.set(true)
                callable.call()
            } catch (e: RuntimeException) {
                throw e
            } catch (e: Exception) {
                throw RuntimeException(e)
            } finally {
                bypassAuthorizationThreadLocal.set(false)
            }
        }
    }
}