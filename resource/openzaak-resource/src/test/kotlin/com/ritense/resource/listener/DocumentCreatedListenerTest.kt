/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import com.ritense.documentenapi.event.DocumentCreated
import com.ritense.openzaak.service.impl.model.documenten.InformatieObject
import com.ritense.resource.service.OpenZaakService
import org.junit.jupiter.api.Test
import java.net.URI
import java.time.LocalDateTime
import kotlin.test.assertEquals

internal class DocumentCreatedListenerTest {

    val openZaakService = mock<OpenZaakService>()
    val documentCreatedListener = DocumentCreatedListener(openZaakService)

    @Test
    fun `should store resource on event`() {
        val captor = argumentCaptor<InformatieObject>()
        val event = DocumentCreated(
            "http://some-url",
            "auteur",
            "file.ext",
            123,
            LocalDateTime.of(2020,7, 2, 5, 2, 11)
        )

        documentCreatedListener.handle(event)

        verify(openZaakService).store(captor.capture())

        val informatieObject = captor.firstValue

        assertEquals("auteur", informatieObject.auteur)
        assertEquals("file.ext", informatieObject.bestandsnaam)
        assertEquals(123, informatieObject.bestandsomvang)
        assertEquals(URI("http://some-url"), informatieObject.url)
        assertEquals(LocalDateTime.of(2020,7, 2, 5, 2, 11), informatieObject.beginRegistratie)
    }
}