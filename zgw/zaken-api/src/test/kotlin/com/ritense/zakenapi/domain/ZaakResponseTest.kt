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
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ZaakResponseTest {
    private val objectMapper = jacksonObjectMapper()
        .findAndRegisterModules()

    @Test
    fun `should deserialize JSON into ZaakResponse`() {
        val zaak: ZaakResponse = objectMapper.readValue(ZAAK_JSON)

        assertNotNull(zaak)
    }

    companion object {
        const val ZAAK_JSON = """
            {
                "url": "http://example.com",
                "uuid": "095be615-a8ad-4c33-8e9c-c7612fbf6c9f",
                "identificatie": "string",
                "bronorganisatie": "002564440",
                "omschrijving": "string",
                "toelichting": "string",
                "zaaktype": "http://example.com",
                "registratiedatum": "2019-08-24",
                "verantwoordelijkeOrganisatie": "002564440",
                "startdatum": "2019-08-24",
                "einddatum": "2019-08-24",
                "einddatumGepland": "2019-08-24",
                "uiterlijkeEinddatumAfdoening": "2019-08-24",
                "publicatiedatum": "2019-08-24",
                "communicatiekanaal": "http://example.com",
                "productenOfDiensten": [
                    "http://example.com"
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
                "selectielijstklasse": "http://example.com",
                "hoofdzaak": "http://example.com",
                "deelzaken": [
                    "http://example.com"
                ],
                "relevanteAndereZaken": [
                    {
                        "url": "http://example.com",
                        "aardRelatie": "vervolg"
                    }
                ],
                "eigenschappen": [
                    "http://example.com"
                ],
                "rollen": [
                    "http://example.com"
                ],
                "status": "http://example.com",
                "zaakinformatieobjecten": [
                    "http://example.com"
                ],
                "zaakobjecten": [
                    "http://example.com"
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
                "resultaat": "http://example.com",
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