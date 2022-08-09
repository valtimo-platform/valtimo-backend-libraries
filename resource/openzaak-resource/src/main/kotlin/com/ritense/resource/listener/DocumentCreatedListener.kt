/*
 * Copyright 2015-2020 Ritense BV, the Netherlands.
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

package com.ritense.resource.listener

import com.ritense.documentenapi.event.DocumentCreated
import com.ritense.openzaak.service.impl.model.documenten.InformatieObject
import com.ritense.resource.service.OpenZaakService
import org.springframework.context.event.EventListener
import java.net.URI

class DocumentCreatedListener(
    private val openZaakService: OpenZaakService
) {
    @EventListener(DocumentCreated::class)
    fun handle(event: DocumentCreated) {
        openZaakService.store(InformatieObject(
            URI(event.url),
            event.auteur,
            event.bestandsnaam,
            event.bestandsomvang,
            event.beginRegistratie
        ))
    }
}