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

import com.ritense.catalogiapi.service.CatalogiService
import com.ritense.catalogiapi.web.rest.result.BesluittypeDto
import com.ritense.catalogiapi.web.rest.result.InformatieobjecttypeDto
import com.ritense.catalogiapi.web.rest.result.ResultaattypeDto
import com.ritense.catalogiapi.web.rest.result.RoltypeDto
import com.ritense.catalogiapi.web.rest.result.StatustypeDto
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@SkipComponentScan
@RequestMapping("/api", produces = [APPLICATION_JSON_UTF8_VALUE])
class CatalogiResource(
    val catalogiService: CatalogiService
) {
    @GetMapping("/v1/documentdefinition/{documentDefinitionName}/zaaktype/documenttype")
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

    @GetMapping("/v1/case-definition/{caseDefinitionName}/zaaktype/roltype")
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

    @GetMapping("/v1/case-definition/{caseDefinitionName}/zaaktype/statustype")
    fun getZaakStatustypen(
        @PathVariable(name = "caseDefinitionName") caseDefinitionName: String
    ): ResponseEntity<List<StatustypeDto>> {
        val zaakStatusTypes = catalogiService.getStatustypen(caseDefinitionName).map {
            StatustypeDto(
                it.url!!,
                it.omschrijving
            )
        }
        return ResponseEntity.ok(zaakStatusTypes)
    }

    @GetMapping("/v1/case-definition/{caseDefinitionName}/zaaktype/resultaattype")
    fun getZaakResultaattypen(
        @PathVariable(name = "caseDefinitionName") caseDefinitionName: String
    ): ResponseEntity<List<ResultaattypeDto>> {
        val zaakResultaatTypes = catalogiService.getResultaattypen(caseDefinitionName).map {
            ResultaattypeDto(
                it.url!!,
                it.omschrijving
            )
        }
        return ResponseEntity.ok(zaakResultaatTypes)
    }

    @GetMapping("/v1/case-definition/{caseDefinitionName}/zaaktype/besluittype")
    fun getZaakBesuilttypen(
        @PathVariable(name = "caseDefinitionName") caseDefinitionName: String
    ): ResponseEntity<List<BesluittypeDto>> {
        val zaakBesluitTypes = catalogiService.getBesluittypen(caseDefinitionName).map {
            BesluittypeDto(
                it.url!!,
                it.omschrijving ?: it.url.toString().substringAfterLast("/")
            )
        }
        return ResponseEntity.ok(zaakBesluitTypes)
    }
}
