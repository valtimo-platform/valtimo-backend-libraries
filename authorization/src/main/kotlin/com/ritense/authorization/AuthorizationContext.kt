package com.ritense.authorization

import java.util.concurrent.Callable


class AuthorizationContext {
    private val bypassAuthorizationThreadLocal = ThreadLocal.withInitial { false }
    private val bypassAuthorization: Boolean
        get() = bypassAuthorizationThreadLocal.get()

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