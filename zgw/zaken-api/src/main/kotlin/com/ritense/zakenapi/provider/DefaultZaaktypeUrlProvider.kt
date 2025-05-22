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

package com.ritense.zakenapi.provider

import com.ritense.catalogiapi.exception.ZaakTypeLinkNotFoundException
import com.ritense.catalogiapi.service.ZaaktypeUrlProvider
import com.ritense.logging.LoggableResource
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.zakenapi.service.ZaakTypeLinkService
import org.springframework.stereotype.Component
import java.net.URI

@Component
@SkipComponentScan
class DefaultZaaktypeUrlProvider(
    private val zaakTypeLinkService: ZaakTypeLinkService
) : ZaaktypeUrlProvider {

    override fun getZaaktypeUrl(
        @LoggableResource("documentDefinitionName") documentDefinitionName: String
    ): URI {
        val zaakTypeLink = zaakTypeLinkService.get(documentDefinitionName)
            ?: throw ZaakTypeLinkNotFoundException("For document definition with name $documentDefinitionName")
        return zaakTypeLink.zaakTypeUrl
    }

    override fun getZaaktypeUrlByCaseDefinitionName(
        @LoggableResource("documentDefinitionName") caseDefinitionName: String
    ): URI {
        val zaakTypeLink = zaakTypeLinkService.get(caseDefinitionName)
            ?: throw ZaakTypeLinkNotFoundException("For case definition with name $caseDefinitionName")
        return zaakTypeLink.zaakTypeUrl
    }
}