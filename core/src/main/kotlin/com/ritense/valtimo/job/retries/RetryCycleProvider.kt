package com.ritense.valtimo.job.retries

import com.ritense.valtimo.contract.annotation.ProcessBean
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component
import java.util.regex.Pattern


@Component
@ProcessBean
class RetryCycleProvider(val config: RetryConfiguration) {

    val standardRetryCycles = mutableMapOf<String, String>()

    init {
        standardRetryCycles[DEFAULT] = "R3/PT1M,PT30M,PT2H"
        standardRetryCycles[QUICK] = "R3/PT30S,PT2M,PT10M"
        standardRetryCycles[CRITICAL] = "R5/PT1M,PT15M,PT4H,PT24H,PT48H"
        standardRetryCycles[DATABASE] = "R4/PT10S,PT1M,PT5M,PT15M" // For connection/lock issues
        standardRetryCycles[EMAIL] = "R3/PT5M,PT20M,PT1H" // For SMTP delivery issues
        standardRetryCycles[WEB_SERVICE] = "R4/PT30S,PT3M,PT8M,PT10M" // For HTTP timeouts/5xx errors
        standardRetryCycles[FILE_TRANSFER] = "R3/PT1M,PT15M,PT1H" // For upload/download issues

        logger.info { "Standard retry cycles: $standardRetryCycles"}
    }

    @PostConstruct
    fun validateRetryCycles() {
        config.cycles.forEach { cycle ->
            if (!RETRY_PATTERN.matcher(cycle.value).matches()) {
                logger.error { "Invalid retry pattern for service ${cycle.key}: ${cycle.value}" }
            }
        }

    }

    fun default(): String {
       return getCycle(DEFAULT)
    }

    fun quick(): String {
       return getCycle(QUICK)
    }

    fun critical(): String {
        return getCycle(CRITICAL)
    }

    fun database(): String {
        return getCycle(DATABASE)
    }

    fun email(): String {
        return getCycle(EMAIL)
    }

    fun webService(): String {
        return getCycle(WEB_SERVICE)
    }

    fun fileTransfer(): String {
        return getCycle(FILE_TRANSFER)
    }

    fun custom(name: String): String? {
       return  config.getCycle(name)
    }

    private fun getCycle(type: String): String {
        if(config.hasCycle(type)) {
            return config.getCycle(type)!!
        }
        return standardRetryCycles[type]!!
    }

    companion object {
        val logger = KotlinLogging.logger {}

        val RETRY_PATTERN = Pattern.compile("^R\\d+/(PT\\d+[HMS],?)+$")

        const val DEFAULT = "default"
        const val QUICK = "quick"
        const val CRITICAL = "critical"
        const val DATABASE = "database"
        const val EMAIL = "email"
        const val WEB_SERVICE = "web-service"
        const val FILE_TRANSFER = "file-transfer"
    }
}