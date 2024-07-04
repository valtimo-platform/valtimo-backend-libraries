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

package com.ritense.zakenapi.mock

import com.ritense.catalogiapi.service.ZaaktypeUrlProvider
import com.ritense.zakenapi.ZaakUrlProvider
import org.springframework.stereotype.Service
import java.net.URI
import java.util.UUID

@Service
class ZaakMockUrlProvider : ZaakUrlProvider, ZaaktypeUrlProvider {

    override fun getZaakUrl(documentId: UUID): URI {
        return URI("http://localhost:56273/zaken/57f66ff6-db7f-43bc-84ef-6847640d3609")
    }

    override fun getZaaktypeUrl(documentDefinitionName: String): URI {
        return URI("http://localhost:56273/catalogi/21c0946a-9058-11ee-b9d1-0242ac120002")
    }

    override fun getZaaktypeUrlByCaseDefinitionName(caseDefinitionName: String): URI {
        return URI("http://localhost:56273/catalogi/251040e8-9058-11ee-b9d1-0242ac120002")
    }
}
