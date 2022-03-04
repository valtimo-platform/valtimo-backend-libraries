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
    fun `Should add user claims to token`() {
        val userId = "myUserId"
        val userToken = Jwts.builder().addClaims(mapOf(
            "test" to "test",
            "realm_access" to mapOf("roles" to arrayOf("ROLE_USER")),
            "resource_access" to mapOf("account" to mapOf("roles" to arrayOf("manage-account"))
        ))).compact()
        val authenticationToken = UsernamePasswordAuthenticationToken(null, userToken)

        Mockito.mockStatic(SecurityUtils::class.java).use { mockedUtils ->
            mockedUtils.`when`<Any> { SecurityUtils.getCurrentUserLogin() }.thenReturn(userId)
            mockedUtils.`when`<Any> { SecurityUtils.getCurrentUserAuthentication() }.thenReturn(authenticationToken)

            val jwt = openZaakTokenGeneratorService.generateToken(SECRET_KEY, CLIENT_ID)
            val claims:Claims = jwtParser.parse(jwt).body as Claims
            assertThat(claims).containsEntry("client_id", CLIENT_ID)
            assertThat(claims).containsEntry("user_id", userId)

            val realmAccess = claims["realm_access"] as Map<String, *>
            assertThat(realmAccess).containsKey("roles")
            val resourceAccess = claims["resource_access"] as Map<String, *>
            assertThat(resourceAccess).containsKey("account")
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