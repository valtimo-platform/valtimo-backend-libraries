package com.ritense.valtimo.job.retries

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "valtimo.process.jobs.retry")
data class RetryConfiguration(
    var cycles: MutableMap<String, String> = mutableMapOf()
) {
    fun getCycle(type: String): String? {
        return cycles[type]
    }

    fun hasCycle(type: String): Boolean {
       return  cycles.keys.contains(type)
    }
}