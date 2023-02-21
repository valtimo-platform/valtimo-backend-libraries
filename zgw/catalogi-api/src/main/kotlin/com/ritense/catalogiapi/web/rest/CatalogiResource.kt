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

package com.ritense.catalogiapi.web.rest

import com.ritense.catalogiapi.web.rest.result.InformatieobjecttypeDto
import com.ritense.catalogiapi.service.CatalogiService
import com.ritense.catalogiapi.web.rest.result.RoltypeDto
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(value = ["/api"])
class CatalogiResource(
    val catalogiService: CatalogiService
) {
    @GetMapping(value = ["/v1/documentdefinition/{documentDefinitionName}/zaaktype/documenttype"])
    fun getZaakObjecttypes(
        @PathVariable(name = "documentDefinitionName") documentDefinitionName: String
    ): ResponseEntity<List<InformatieobjecttypeDto>> {
        val zaakObjectTypes = catalogiService.getInformatieobjecttypes(documentDefinitionName).map {
            InformatieobjecttypeDto(
                it.url!!,
                it.omschrijving
            )
        }
        return ResponseEntity.ok(zaakObjectTypes)
    }

    @GetMapping(value = ["/v1/case-definition/{caseDefinitionName}/zaaktype/roltype"])
    fun getZaakRoltypes(
        @PathVariable(name = "caseDefinitionName") caseDefinitionName: String
    ): ResponseEntity<List<RoltypeDto>> {
        val zaakRolTypes = catalogiService.getRoltypes(caseDefinitionName).map {
            RoltypeDto(
                it.url,
                it.omschrijving
            )
        }
        return ResponseEntity.ok(zaakRolTypes)
    }
}
