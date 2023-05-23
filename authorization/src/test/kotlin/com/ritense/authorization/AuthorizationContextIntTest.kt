package com.ritense.authorization

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired

class AuthorizationContextIntTest @Autowired constructor(
    private val authorizationService: AuthorizationService
) : BaseIntegrationTest() {
    @Test
    fun `should succeed when ran without authorization`() {
        assertTrue(isAuthorizedWithRunWithoutAuthorization())
    }

    @Test
    fun `should throw RuntimeException when ran with authorization`() {
        assertThrows<RuntimeException> { isAuthorizedWithoutRunWithoutAuthorization() }
    }

    fun isAuthorizedWithRunWithoutAuthorization(): Boolean {
        return AuthorizationContext.runWithoutAuthorization {
            isAuthorizedWithoutRunWithoutAuthorization()
        }
    }

    fun isAuthorizedWithoutRunWithoutAuthorization(): Boolean {
        authorizationService
            .requirePermission(
                AuthorizationRequest(
                    String::class.java,
                    action = Action.VIEW),
                "test",
                null
            )

        return true
    }
}