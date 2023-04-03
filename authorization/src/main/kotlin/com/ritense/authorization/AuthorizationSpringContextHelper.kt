package com.ritense.authorization

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware

class AuthorizationSpringContextHelper : ApplicationContextAware {

    override fun setApplicationContext(context: ApplicationContext) {
        Companion.context = context
    }

    companion object {
        private var context: ApplicationContext? = null
        private var authorizationService: AuthorizationService? = null

        fun getService(): AuthorizationService {
            authorizationService = authorizationService?: context!!.getBean(AuthorizationService::class.java)
            return authorizationService!!
        }
    }
}