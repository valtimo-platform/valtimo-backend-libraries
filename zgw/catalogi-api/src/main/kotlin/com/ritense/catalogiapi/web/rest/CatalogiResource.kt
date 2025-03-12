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

package com.ritense.catalogiapi.web.rest

import com.ritense.catalogiapi.service.CatalogiService
import com.ritense.catalogiapi.web.rest.result.BesluittypeDto
import com.ritense.catalogiapi.web.rest.result.EigenschapDto
import com.ritense.catalogiapi.web.rest.result.InformatieobjecttypeDto
import com.ritense.catalogiapi.web.rest.result.ResultaattypeDto
import com.ritense.catalogiapi.web.rest.result.RoltypeDto
import com.ritense.catalogiapi.web.rest.result.StatustypeDto
import com.ritense.catalogiapi.web.rest.result.ZaaktypeDto
import com.ritense.logging.LoggableResource
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.case_.CaseDefinitionId
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
    private val catalogiService: CatalogiService
) {
    @GetMapping("/v1/case-definition/{caseDefinitionKey}/version/{versionTag}/zaaktype/documenttype")
    fun getZaakObjecttypes(
        @LoggableResource("caseDefinitionKey") @PathVariable(name = "caseDefinitionKey") caseDefinitionKey: String,
        @LoggableResource("versionTag") @PathVariable(name = "versionTag") versionTag: String,
    ): ResponseEntity<List<InformatieobjecttypeDto>> {
        val zaakObjectTypes =
            catalogiService.getInformatieobjecttypes(CaseDefinitionId(caseDefinitionKey, versionTag)).map {
                InformatieobjecttypeDto(
                    it.url!!,
                    it.omschrijving
                )
            }
        return ResponseEntity.ok(zaakObjectTypes)
    }

    @GetMapping("/v1/case-definition/{caseDefinitionKey}/version/{versionTag}/zaaktype/roltype")
    fun getZaakRoltypes(
        @LoggableResource("caseDefinitionKey") @PathVariable(name = "caseDefinitionKey") caseDefinitionKey: String,
        @LoggableResource("versionTag") @PathVariable(name = "versionTag") versionTag: String,
    ): ResponseEntity<List<RoltypeDto>> {
        val zaakRolTypes = catalogiService.getRoltypes(CaseDefinitionId(caseDefinitionKey, versionTag)).map {
            RoltypeDto(
                it.url,
                it.omschrijving
            )
        }
        return ResponseEntity.ok(zaakRolTypes)
    }

    @GetMapping("/v1/case-definition/{caseDefinitionKey}/version/{versionTag}/zaaktype/statustype")
    fun getZaakStatustypen(
        @LoggableResource("caseDefinitionKey") @PathVariable(name = "caseDefinitionKey") caseDefinitionKey: String,
        @LoggableResource("versionTag") @PathVariable(name = "versionTag") versionTag: String,
    ): ResponseEntity<List<StatustypeDto>> {
        val zaakStatusTypes = catalogiService.getStatustypen(CaseDefinitionId(caseDefinitionKey, versionTag)).map {
            StatustypeDto(
                it.url!!,
                it.omschrijving
            )
        }
        return ResponseEntity.ok(zaakStatusTypes)
    }

    @GetMapping("/v1/case-definition/{caseDefinitionKey}/version/{versionTag}/zaaktype/resultaattype")
    fun getZaakResultaattypen(
        @LoggableResource("caseDefinitionKey") @PathVariable(name = "caseDefinitionKey") caseDefinitionKey: String,
        @LoggableResource("versionTag") @PathVariable(name = "versionTag") versionTag: String,
    ): ResponseEntity<List<ResultaattypeDto>> {
        val zaakResultaatTypes =
            catalogiService.getResultaattypen(CaseDefinitionId(caseDefinitionKey, versionTag)).map {
                ResultaattypeDto(
                    it.url!!,
                    it.omschrijving
                )
            }
        return ResponseEntity.ok(zaakResultaatTypes)
    }

    @GetMapping("/v1/case-definition/{caseDefinitionKey}/version/{versionTag}/zaaktype/besluittype")
    fun getZaakBesuilttypen(
        @LoggableResource("caseDefinitionKey") @PathVariable(name = "caseDefinitionKey") caseDefinitionKey: String,
        @LoggableResource("versionTag") @PathVariable(name = "versionTag") versionTag: String,
    ): ResponseEntity<List<BesluittypeDto>> {
        val zaakBesluitTypes = catalogiService.getBesluittypen(CaseDefinitionId(caseDefinitionKey, versionTag)).map {
            BesluittypeDto(
                it.url!!,
                it.omschrijving ?: it.url.toString().substringAfterLast("/")
            )
        }
        return ResponseEntity.ok(zaakBesluitTypes)
    }

    @GetMapping("/management/v1/zgw/zaaktype")
    fun getZaakTypen(): ResponseEntity<List<ZaaktypeDto>> {
        val zaakTypen = catalogiService.getZaakTypen().map { ZaaktypeDto.of(it) }
        return ResponseEntity.ok(zaakTypen)
    }

    @GetMapping("/management/v1/case-definition/{caseDefinitionKey}/version/{versionTag}/catalogi-eigenschappen")
    fun getEigenschappen(
        @LoggableResource("caseDefinitionKey") @PathVariable(name = "caseDefinitionKey") caseDefinitionKey: String,
        @LoggableResource("versionTag") @PathVariable(name = "versionTag") versionTag: String
    ): ResponseEntity<List<EigenschapDto>> {
        val eigenschappen = catalogiService.getEigenschappen(CaseDefinitionId(caseDefinitionKey, versionTag))
            .map { EigenschapDto.of(it) }
            .sortedBy { it.name }
  
        return ResponseEntity.ok(eigenschappen)
    }
}
