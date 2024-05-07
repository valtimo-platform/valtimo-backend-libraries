package com.ritense.formviewmodel

import mu.KotlinLogging
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware

class SpringContextHelper : ApplicationContextAware {

    override fun setApplicationContext(context: ApplicationContext) {
        // Store ApplicationContext reference to access required beans later on.
        Companion.context = context
    }

    companion object {
        private val logger = KotlinLogging.logger {}
        private var context: ApplicationContext? = null

        /**
         * Returns the Spring managed bean instance of the given class type if it exists.
         * Returns exception otherwise.
         * @param beanClass
         * @return Object
         */
        fun <T : Any?> getBean(beanClass: Class<T>): T {
            logger.trace { "Retrieving bean $beanClass" }
            return context?.getBean(beanClass) ?: throw IllegalStateException("Cannot getBean $beanClass").also {
                logger.error { it.message }
            }
        }

    }

}