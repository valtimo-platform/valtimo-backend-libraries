/*
 *
 *  Copyright 2015-2025 Ritense BV, the Netherlands.
 *
 *  Licensed under EUPL, Version 1.2 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.ritense.valtimo.job.retries

import com.ritense.valtimo.job.retries.RetryCycleProvider.Companion.CRITICAL
import com.ritense.valtimo.job.retries.RetryCycleProvider.Companion.DEFAULT
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import kotlin.collections.set

@ExtendWith(MockitoExtension::class)
class RetryCycleProviderTest {

    private lateinit var provider: RetryCycleProvider

    @BeforeEach
    fun before() {
        val config = RetryConfiguration()
        config.cycles[CRITICAL] = "R3/PT1M,PT15M,PT4H"
        config.cycles[DEFAULT] = "R1/PT1M"
        config.cycles["customCycle"] = "R1/PT1M"
        config.cycles["errorCycle"] = "PT1M"
        provider = RetryCycleProvider(config)
    }


    @Test
    @ExtendWith(OutputCaptureExtension::class)
    fun shouldValidateRetryCycles(capturedOutput: CapturedOutput) {
        provider.validateRetryCycles()
        assertThat(capturedOutput.out).isNotNull().contains("errorCycle")
    }

    @Test
    fun shouldUseDefaultFromConfig() {
        assertEquals("R1/PT1M", provider.default())
    }

    @Test
    fun shouldUseCriticalFromConfig() {
        assertEquals("R3/PT1M,PT15M,PT4H", provider.critical())
    }

    @Test
    fun shouldUseQuickFromStandard() {
        assertEquals("R3/PT30S,PT2M,PT10M", provider.quick())
    }

    @Test
    fun shouldGetCustomCycle() {
        assertEquals("R1/PT1M", provider.custom("customCycle"))
    }
}