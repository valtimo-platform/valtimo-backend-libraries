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

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired

class AuthorizationServiceIntTest @Autowired constructor(
    private val authorizationService: AuthorizationService
) : BaseIntegrationTest() {
    @Test
    fun `should succeed when ran without authorization`() {
        val result = AuthorizationContext.getWithoutAuthorization {
            requirePermission()
        }
        assertTrue(result)
    }

    @Test
    fun `should throw RuntimeException when ran with authorization`() {
        assertThrows<RuntimeException> {
            requirePermission()
        }
    }

    @Test
    fun `should throw RuntimeException when action is DENY`() {
        assertThrows<RuntimeException> {
            requirePermission(Action.DENY)
        }
    }

    @Test
    fun `should succeed when ran without authorization when action is DENY`() {
        val result = AuthorizationContext.getWithoutAuthorization {
            requirePermission(Action.DENY)
        }
        assertTrue(result)
    }

    fun requirePermission(action: Action = Action.VIEW): Boolean {
        authorizationService
            .requirePermission(
                AuthorizationRequest(
                    String::class.java,
                    action = action),
                "test"
            )

        return true
    }
}