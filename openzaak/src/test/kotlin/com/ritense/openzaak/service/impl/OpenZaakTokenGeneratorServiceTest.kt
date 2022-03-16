package com.ritense.openzaak.service.impl

import com.ritense.valtimo.contract.utils.SecurityUtils
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.impl.DefaultJwtParserBuilder
import io.jsonwebtoken.security.Keys
import java.nio.charset.Charset
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken


internal class OpenZaakTokenGeneratorServiceTest {

    companion object {
        const val SECRET_KEY = "5N7Q8R9TBUCVEXFYG2J3K4M6P7Q8SATBUDWEXFZH2J3M5N6P8R9SAUCVDW"
        const val CLIENT_ID = "testClient"
    }
    private val openZaakTokenGeneratorService = OpenZaakTokenGeneratorService()
    private val jwtParser = DefaultJwtParserBuilder()
        .setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY.toByteArray(Charset.forName("UTF-8"))))
        .build()


    @Test
    fun `Should generate async (Valtimo) token`() {
        val jwt = openZaakTokenGeneratorService.generateToken(SECRET_KEY, CLIENT_ID)
        val claims:Claims = jwtParser.parse(jwt).body as Claims
        assertThat(claims).containsEntry("client_id", CLIENT_ID)
        assertThat(claims).containsEntry("user_id", "Valtimo")
    }

    @Test
    fun `Should generate user token`() {
        val userId = "myUserId"
        Mockito.mockStatic(SecurityUtils::class.java).use { mockedUtils ->
            mockedUtils.`when`<Any> { SecurityUtils.getCurrentUserLogin() }.thenReturn(userId)

            val jwt = openZaakTokenGeneratorService.generateToken(SECRET_KEY, CLIENT_ID)
            val claims:Claims = jwtParser.parse(jwt).body as Claims
            assertThat(claims).containsEntry("client_id", CLIENT_ID)
            assertThat(claims).containsEntry("user_id", userId)
        }
    }

    @Test
    fun `Should add user roles to token`() {
        val userId = "myUserId"
        val roles = listOf("ROLE_USER", "ROLE_ADMIN")

        Mockito.mockStatic(SecurityUtils::class.java).use { mockedUtils ->
            mockedUtils.`when`<Any> { SecurityUtils.getCurrentUserLogin() }.thenReturn(userId)
            mockedUtils.`when`<Any> { SecurityUtils.getCurrentUserRoles() }.thenReturn(roles)

            val jwt = openZaakTokenGeneratorService.generateToken(SECRET_KEY, CLIENT_ID)
            val claims:Claims = jwtParser.parse(jwt).body as Claims
            assertThat(claims).containsEntry("client_id", CLIENT_ID)
            assertThat(claims).containsEntry("user_id", userId)

            val claimedRoles = claims["roles"] as List<String>
            assertThat(claimedRoles).containsExactlyInAnyOrder(*roles.toTypedArray())
            assertThat(claims).doesNotContainKey("test")
        }
    }

    @Test
    fun `Should not crash on invalid credentials (JWT) value`() {
        val userId = "myUserId"
        val userToken = "a.b.c.d"
        val authenticationToken = UsernamePasswordAuthenticationToken(null, userToken)

        Mockito.mockStatic(SecurityUtils::class.java).use { mockedUtils ->
            mockedUtils.`when`<Any> { SecurityUtils.getCurrentUserLogin() }.thenReturn(userId)
            mockedUtils.`when`<Any> { SecurityUtils.getCurrentUserAuthentication() }.thenReturn(authenticationToken)

            val jwt = openZaakTokenGeneratorService.generateToken(SECRET_KEY, CLIENT_ID)
            val claims:Claims = jwtParser.parse(jwt).body as Claims
            assertThat(claims).containsEntry("client_id", CLIENT_ID)
            assertThat(claims).containsEntry("user_id", userId)
            assertThat(claims).doesNotContainKey("test")
            assertThat(claims).doesNotContainKey("realm_access")
            assertThat(claims).doesNotContainKey("resource_access")
        }
    }
}