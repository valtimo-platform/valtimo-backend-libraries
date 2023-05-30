/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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

package com.ritense.authorization

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class AuthorizationContextTest {

    @Test
    fun `should run callable with return type from runWithoutAuthorization`() {
        val result = AuthorizationContext.runWithoutAuthorization {
            AuthorizationContext.ignoreAuthorization
        }

        assertEquals(true, result)
        assertEquals(false, AuthorizationContext.ignoreAuthorization)
    }

    @Test
    fun `should run callable without return type from runWithoutAuthorization`() {
        val result = AuthorizationContext.runWithoutAuthorization {
            assertEquals(true, AuthorizationContext.ignoreAuthorization)
        }

        assertEquals(true, result is Unit)
        assertEquals(false, AuthorizationContext.ignoreAuthorization)
    }
}