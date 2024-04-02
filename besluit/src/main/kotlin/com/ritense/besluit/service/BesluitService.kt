/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

package com.ritense.besluit.service

import com.ritense.besluit.domain.BesluitType
import com.ritense.connector.service.ConnectorService
import com.ritense.openzaak.catalogi.CatalogiClient
import com.ritense.openzaak.domain.connector.OpenZaakConnector
import java.net.URI

@Deprecated("Since 12.0.0. Please use the Besluiten API module instead.")
open class BesluitService(
    private val catalogiClient: CatalogiClient,
    private val connectorService: ConnectorService,
) {

    @Deprecated("Since 12.0.0", ReplaceWith("com.ritense.catalogiapi.CatalogiApiPlugin.getBesluittypen()"))
    fun getBesluittypen(): List<BesluitType> {
        val openZaakConnector = connectorService.loadByClassName(OpenZaakConnector::class.java)
        val catalogusUrl = URI(openZaakConnector.getProperties().openZaakConfig.catalogusUrl)
        return catalogiClient.getBesluittypen(catalogusUrl).results
            .map { BesluitType(it.url, it.omschrijving) }
    }
}