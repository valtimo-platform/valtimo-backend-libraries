package com.ritense.valtimo.web.autoconfigure

import com.ritense.valtimo.web.client.ApacheRequestFactoryCustomizer
import com.ritense.valtimo.web.client.DefaultErrorHandlingCustomizer
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean

@AutoConfiguration
class RestClientAutoConfiguration {

    @Bean
    fun requestFactoryCustomizer(): ApacheRequestFactoryCustomizer {
        return ApacheRequestFactoryCustomizer()
    }

    @Bean
    fun defaultErrorHandlingCustomizer(): DefaultErrorHandlingCustomizer {
        return DefaultErrorHandlingCustomizer()
    }

}