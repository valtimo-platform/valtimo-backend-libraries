package com.ritense.valtimo.job.retries

import com.ritense.valtimo.BaseIntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.assertEquals


class RetryCycleProviderIntTest @Autowired constructor(
    private val retryCycleProvider: RetryCycleProvider): BaseIntegrationTest() {

    @Test
    fun shouldLoadCyclesFromYml() {
        assertEquals("R3/PT1M,PT15M,PT1H", retryCycleProvider.critical())
        assertEquals("R4/PT10S,PT1M,PT5M,PT15M", retryCycleProvider.default())
    }
}