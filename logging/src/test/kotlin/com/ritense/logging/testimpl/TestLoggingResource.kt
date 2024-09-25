/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
 *
 * Licensed under EUPL, Version 1.2 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ritense.logging.testimpl

import com.ritense.logging.withLoggingContext
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api", produces = [APPLICATION_JSON_UTF8_VALUE])
class TestLoggingResource {

    @Transactional(readOnly = true)
    @GetMapping("/v1/test-error")
    fun throwTestErrorWithContext(
    ): ResponseEntity<Any> {
        try {
            withLoggingContext("irrelevant key" to "irrelevant value") {
                throw IllegalStateException("irrelevant-test-error")
            }
        } catch (e: Exception) {
            // ignored
        }
        withLoggingContext("outer key" to "outer value") {
            try {
                withLoggingContext("inner key" to "inner value") {
                    throw IllegalStateException("inner-test-error")
                }
            } catch (e: Exception) {
                throw IllegalStateException("outer-test-error", e)
            }
        }
    }

}
