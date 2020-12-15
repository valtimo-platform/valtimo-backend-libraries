/*
 * Copyright 2020 Dimpact.
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

package com.ritense.openzaak.service

import com.ritense.openzaak.service.impl.OpenZaakTokenGeneratorService
import io.jsonwebtoken.Jwts
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.charset.Charset

class TokenGeneratorServiceTest {

    private val openZaakTokenGeneratorService = OpenZaakTokenGeneratorService()

    @Test
    fun `should generate token`() {
        val testSecretKey = "ySCrWMK7nCPdoSkjydb58racw2tOzuDqgge3SFhgR3Fe"

        val generatedToken = openZaakTokenGeneratorService.generateToken(
            testSecretKey,
            "testClientId"
        )

        val claims = Jwts.parserBuilder()
            .setSigningKey(testSecretKey.toByteArray(Charset.forName("UTF-8")))
            .build()
            .parseClaimsJws(generatedToken)

        assertThat(claims.body.issuer).isEqualTo("testClientId")
        assertThat(claims.body.get("client_id")).isEqualTo("testClientId")
        assertThat(claims.body.get("user_id")).isEqualTo("Valtimo")
        assertThat(claims.body.get("user_representation")).isEqualTo("")
    }

}
