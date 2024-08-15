package com.ritense.processlink.url.domain

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "valtimo.processlink")
data class URLVariables(
    var url: Map<String, String>
)