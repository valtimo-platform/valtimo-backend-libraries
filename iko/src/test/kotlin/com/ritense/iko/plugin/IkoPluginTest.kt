/*
 * Copyright 2015-2025 Ritense BV, the Netherlands.
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

package com.ritense.iko.plugin

import com.ritense.iko.client.IkoClient
import com.ritense.valtimo.contract.json.MapperSingleton
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.net.URI
import kotlin.test.assertEquals

internal class IkoPluginTest {

    @Test
    fun `should search JSON`() {
        val ikoClient: IkoClient = mock()
        val plugin = IkoPlugin(
            ikoClient,
        )
        plugin.url = URI("https://zaken.plugin.url")
        whenever(ikoClient.search(any(), any(), any(), any()))
            .thenReturn(
                MapperSingleton.get().readTree(
                    """
                {
                    "type": "ZoekMetGeslachtsnaamEnGeboortedatum",
                    "personen": [
                        {
                            "burgerservicenummer": "999993653",
                            "naam": {
                                "voornamen": "Suzanne",
                                "geslachtsnaam": "Moulin",
                                "voorletters": "S.",
                                "volledigeNaam": "Suzanne Moulin",
                                "aanduidingNaamgebruik": {
                                    "code": "E",
                                    "omschrijving": "eigen geslachtsnaam"
                                }
                            }
                        }
                    ]
                }
            """.trimIndent()
                )
            )

        val result = plugin.search(
            endpointPath = "personen",
            endpointType = "ZoekMetGeslachtsnaamEnGeboortedatum",
            filters = mapOf(
                "geslachtsnaam" to "Moulin",
                "geboortedatum" to "1985-12-01",
            )
        )

        verify(ikoClient).search(
            any(),
            eq("personen"),
            eq("ZoekMetGeslachtsnaamEnGeboortedatum"),
            eq(mapOf("geslachtsnaam" to "Moulin", "geboortedatum" to "1985-12-01"))
        )
        assertEquals("ZoekMetGeslachtsnaamEnGeboortedatum", result["type"].asText())
        assertEquals("999993653", result["personen"][0]["burgerservicenummer"].asText())
        assertEquals("Suzanne Moulin", result["personen"][0]["naam"]["volledigeNaam"].asText())
    }
}