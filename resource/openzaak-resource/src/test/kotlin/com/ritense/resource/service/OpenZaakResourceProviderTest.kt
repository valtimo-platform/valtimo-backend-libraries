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

package com.ritense.resource.service

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.ritense.resource.domain.OpenZaakResource
import org.junit.jupiter.api.Test
import java.net.URI
import kotlin.test.assertEquals

internal class OpenZaakResourceProviderTest {

    val openZaakService = mock<OpenZaakService>()
    val openZaakResourceProvider = OpenZaakResourceProvider(openZaakService)

    @Test
    fun `should get resource by id`() {
        val documentUrl = "http://document.url"
        val resourceMock = mock<OpenZaakResource>()

        whenever(openZaakService.getResourceByInformatieObjectUrl(URI(documentUrl))).thenReturn(resourceMock)

        val returnedResource = openZaakResourceProvider.getResource(documentUrl)

        assertEquals(resourceMock, returnedResource)
        verify(openZaakService).getResourceByInformatieObjectUrl(URI(documentUrl))
    }
}