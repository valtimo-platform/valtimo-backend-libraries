package com.ritense.tenancy.autoconfigure

import com.ritense.tenancy.jpa.TenantAwareListener
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TenantAwareListenerAutoConfiguration {

    @Bean
    fun tenantAwareListener() = TenantAwareListener()

}