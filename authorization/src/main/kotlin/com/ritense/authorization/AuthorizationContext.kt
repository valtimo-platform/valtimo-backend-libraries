package com.ritense.authorization

import java.util.function.Supplier


class AuthorizationContext {

    companion object {
        private val ignoreAuthorizationThreadLocal = ThreadLocal.withInitial { false }

        @JvmStatic
        val ignoreAuthorization: Boolean
            get() = ignoreAuthorizationThreadLocal.get()

        @JvmStatic
        fun runWithoutAuthorization(runnable: Runnable) {
            return try {
                ignoreAuthorizationThreadLocal.set(true)
                runnable.run()
            } finally {
                ignoreAuthorizationThreadLocal.set(false)
            }
        }

        @JvmStatic
        fun <T> getWithoutAuthorization(supplier: Supplier<T>): T {
            return try {
                ignoreAuthorizationThreadLocal.set(true)
                supplier.get()
            } finally {
                ignoreAuthorizationThreadLocal.set(false)
            }
        }
    }
}