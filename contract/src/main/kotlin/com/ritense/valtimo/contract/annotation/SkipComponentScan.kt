package com.ritense.valtimo.contract.annotation

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression

/**
 * The issue and implemented workaround is described in https://github.com/spring-projects/spring-framework/issues/28978
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ConditionalOnExpression("false")
annotation class SkipComponentScan
