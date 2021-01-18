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

package com.ritense.openzaak.domain.configuration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.util.UUID
import javax.validation.ConstraintViolationException

class OpenZaakConfigTest {

    private val id = OpenZaakConfigId.newId(UUID.randomUUID())
    private val url = "www.zaakurl.nl"
    private val clientId = "client id"
    private val secret = Secret("value")
    private val rsin = Rsin("002564440")

    @Test
    fun `should not create entity`() {
        assertThrows(ConstraintViolationException::class.java) {
            OpenZaakConfig(id,
                "tooooloooooooonnngggggg-tooooloooooooonnngggggg-tooooloooooooonnngggggg-tooooloooooooonnngggggg-tooooloooooooonnngggggg"
                    + "tooooloooooooonnngggggg-tooooloooooooonnngggggg-tooooloooooooonnngggggg-tooooloooooooonnngggggg-tooooloooooooonnngggggg"
                    + "tooooloooooooonnngggggg-tooooloooooooonnngggggg-tooooloooooooonnngggggg-tooooloooooooonnngggggg-tooooloooooooonnngggggg"
                    + "tooooloooooooonnngggggg-tooooloooooooonnngggggg-tooooloooooooonnngggggg-tooooloooooooonnngggggg-tooooloooooooonnngggggg"
                    + "tooooloooooooonnngggggg-tooooloooooooonnngggggg-tooooloooooooonnngggggg-tooooloooooooonnngggggg-tooooloooooooonnngggggg"
                    + "tooooloooooooonnngggggg-tooooloooooooonnngggggg-tooooloooooooonnngggggg-tooooloooooooonnngggggg-tooooloooooooonnngggggg"
                    + "tooooloooooooonnngggggg-tooooloooooooonnngggggg-tooooloooooooonnngggggg-tooooloooooooonnngggggg-tooooloooooooonnngggggg"
                    + "tooooloooooooonnngggggg-tooooloooooooonnngggggg-tooooloooooooonnngggggg-tooooloooooooonnngggggg-tooooloooooooonnngggggg"
                    + "tooooloooooooonnngggggg-tooooloooooooonnngggggg-tooooloooooooonnngggggg-tooooloooooooonnngggggg-tooooloooooooonnngggggg"
                    + "tooooloooooooonnngggggg-tooooloooooooonnngggggg-tooooloooooooonnngggggg-tooooloooooooonnngggggg-tooooloooooooonnngggggg"
                    + "tooooloooooooonnngggggg-tooooloooooooonnngggggg-tooooloooooooonnngggggg-tooooloooooooonnngggggg-tooooloooooooonnngggggg",
                clientId,
                secret,
                rsin
            )
        }
    }

    @Test
    fun `should create entity`() {
        val openZaakConfig = OpenZaakConfig(
            id,
            url,
            clientId,
            secret,
            rsin
        )

        assertThat(openZaakConfig.id).isEqualTo(id)
        assertThat(openZaakConfig.url).isEqualTo(url)
        assertThat(openZaakConfig.clientId).isEqualTo(clientId)
        assertThat(openZaakConfig.secret).isEqualTo(secret)
        assertThat(openZaakConfig.rsin).isEqualTo(rsin)
    }

}