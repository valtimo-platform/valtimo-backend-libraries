package com.ritense.authorization

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AuthorizationConfiguration {
    @Bean
    fun springContextHelper(): AuthorizationSpringContextHelper {
        return AuthorizationSpringContextHelper()
    }


}