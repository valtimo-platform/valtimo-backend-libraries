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

package com.ritense.zakenapi.web.rest

import com.ritense.document.domain.RelatedFile
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
import com.ritense.zakenapi.domain.ZaakResponse
import com.ritense.zakenapi.service.ZaakDocumentService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping(value = ["/api/v1/zaken-api/document/{documentId}"], produces = [APPLICATION_JSON_UTF8_VALUE])
class ZaakDocumentResource(
    private val zaakDocumentService: ZaakDocumentService
) {

    @GetMapping("/files")
    fun getFiles(@PathVariable(name = "documentId") documentId: UUID): List<RelatedFile> {
        return zaakDocumentService.getInformatieObjectenAsRelatedFiles(documentId)
    }

    @GetMapping("/zaak")
    fun getZaakMetadata(@PathVariable(name = "documentId") documentId: UUID): ZaakResponse? {
        return zaakDocumentService.getZaakByDocumentId(documentId)
    }
}
