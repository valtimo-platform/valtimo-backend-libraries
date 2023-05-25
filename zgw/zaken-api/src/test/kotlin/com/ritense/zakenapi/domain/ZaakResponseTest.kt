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

package com.ritense.zakenapi.domain

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class ZaakResponseTest {
    private val objectMapper = jacksonObjectMapper()
        .findAndRegisterModules()

    @Test
    fun `should deserialize JSON into ZaakResponse`() {
        val zaak: ZaakResponse = objectMapper.readValue(ZAAK_JSON)

        assertNotNull(zaak)
        assertEquals(Betalingsindicatie.NVT, zaak.betalingsindicatie)
    }

    companion object {
        const val ZAAK_JSON = """
            {
                "url": "https://example.com",
                "uuid": "095be615-a8ad-4c33-8e9c-c7612fbf6c9f",
                "identificatie": "string",
                "bronorganisatie": "002564440",
                "omschrijving": "string",
                "toelichting": "string",
                "zaaktype": "https://example.com",
                "registratiedatum": "2019-08-24",
                "verantwoordelijkeOrganisatie": "002564440",
                "startdatum": "2019-08-24",
                "einddatum": "2019-08-24",
                "einddatumGepland": "2019-08-24",
                "uiterlijkeEinddatumAfdoening": "2019-08-24",
                "publicatiedatum": "2019-08-24",
                "communicatiekanaal": "https://example.com",
                "productenOfDiensten": [
                    "https://example.com"
                ],
                "vertrouwelijkheidaanduiding": "openbaar",
                "betalingsindicatie": "nvt",
                "betalingsindicatieWeergave": "string",
                "laatsteBetaaldatum": "2019-08-24T14:15:22Z",
                "zaakgeometrie": {
                    "type": "Point",
                    "coordinates": [
                        0,
                        0
                    ]
                },
                "verlenging": {
                    "reden": "string",
                    "duur": "string"
                },
                "opschorting": {
                    "indicatie": true,
                    "reden": "string"
                },
                "selectielijstklasse": "https://example.com",
                "hoofdzaak": "https://example.com",
                "deelzaken": [
                    "https://example.com"
                ],
                "relevanteAndereZaken": [
                    {
                        "url": "https://example.com",
                        "aardRelatie": "vervolg"
                    }
                ],
                "eigenschappen": [
                    "https://example.com"
                ],
                "rollen": [
                    "https://example.com"
                ],
                "status": "https://example.com",
                "zaakinformatieobjecten": [
                    "https://example.com"
                ],
                "zaakobjecten": [
                    "https://example.com"
                ],
                "kenmerken": [
                    {
                        "kenmerk": "string",
                        "bron": "string"
                    }
                ],
                "archiefnominatie": "blijvend_bewaren",
                "archiefstatus": "nog_te_archiveren",
                "archiefactiedatum": "2019-08-24",
                "resultaat": "https://example.com",
                "opdrachtgevendeOrganisatie": "string",
                "processobjectaard": "string",
                "resultaattoelichting": "string",
                "startdatumBewaartermijn": "2019-08-24",
                "processobject": {
                    "datumkenmerk": "string",
                    "identificatie": "string",
                    "objecttype": "string",
                    "registratie": "string"
                }
            }
        """
    }
}