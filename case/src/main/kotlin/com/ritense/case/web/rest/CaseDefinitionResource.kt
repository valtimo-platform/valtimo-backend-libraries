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

package com.ritense.case.web.rest

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.authorization.annotation.RunWithoutAuthorization
import com.ritense.case.exception.UnknownCaseDefinitionException
import com.ritense.case.service.CaseDefinitionService
import com.ritense.case.web.rest.dto.CaseDefinitionDraftCreateRequest
import com.ritense.case.web.rest.dto.CaseDefinitionResponseDto
import com.ritense.case.web.rest.dto.CaseDefinitionSettingsResponseDto
import com.ritense.case.web.rest.dto.CaseListColumnDto
import com.ritense.case.web.rest.dto.CaseSettingsDto
import com.ritense.case_.repository.CaseDefinitionRepository
import com.ritense.case_.service.ActiveCaseDefinitionService
import com.ritense.exporter.ExportService
import com.ritense.exporter.request.CaseDefinitionExportRequest
import com.ritense.importer.ImportService
import com.ritense.importer.exception.ImportServiceException
import com.ritense.logging.LoggableResource
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
import mu.KotlinLogging
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Controller
@SkipComponentScan
@RequestMapping("/api", produces = [APPLICATION_JSON_UTF8_VALUE])
class CaseDefinitionResource(
    private val service: CaseDefinitionService,
    private val activeCaseDefinitionService: ActiveCaseDefinitionService,
    private val exportService: ExportService,
    private val importService: ImportService,
    private val caseDefinitionRepository: CaseDefinitionRepository,
) {

    @RunWithoutAuthorization
    @GetMapping("/management/v1/case-definition/{caseDefinitionKey}/version/{versionTag}")
    fun getCaseDefinition(
        @LoggableResource("caseDefinitionKey") @PathVariable caseDefinitionKey: String,
        @LoggableResource("versionTag") @PathVariable versionTag: String,
    ): ResponseEntity<CaseDefinitionResponseDto> {
        val caseDefinition = service.getCaseDefinition(CaseDefinitionId.of(caseDefinitionKey, versionTag))
        val similarCaseDefinitions = caseDefinition.basedOnVersionTag?.let {
            service.getCaseDefinitionsBasedOnVersion(caseDefinitionKey, caseDefinition.basedOnVersionTag)
                .filter { it.id != caseDefinition.id }
        } ?: emptyList()
        val conflictingVersions = if (similarCaseDefinitions.isNotEmpty()) {
            similarCaseDefinitions.joinToString { it.id.versionTag.toString() }
        } else {
            null
        }
        return ResponseEntity.ok(CaseDefinitionResponseDto.of(caseDefinition, conflictingVersions))
    }

    @RunWithoutAuthorization
    @PostMapping("/management/v1/case-definition/{caseDefinitionKey}/version/{versionTag}/draft")
    fun createCaseDefinitionDraft(
        @LoggableResource("caseDefinitionKey") @PathVariable caseDefinitionKey: String,
        @LoggableResource("versionTag") @PathVariable versionTag: String,
        @RequestBody request: CaseDefinitionDraftCreateRequest
    ): ResponseEntity<CaseDefinitionResponseDto> {
        return ResponseEntity.ok(
            CaseDefinitionResponseDto.of(
                service.createCaseDefinitionDraft(
                    CaseDefinitionId.of(
                        caseDefinitionKey,
                        versionTag
                    ), request
                )
            )
        )
    }

    @RunWithoutAuthorization
    @DeleteMapping("/management/v1/case-definition/{caseDefinitionKey}/version/{versionTag}")
    fun deleteCaseDefinition(
        @LoggableResource("caseDefinitionKey") @PathVariable caseDefinitionKey: String,
        @LoggableResource("versionTag") @PathVariable versionTag: String,
    ): ResponseEntity<Unit> {
        service.deleteCaseDefinition(CaseDefinitionId.of(caseDefinitionKey, versionTag))
        return ResponseEntity.ok().build()
    }

    @GetMapping("/management/v1/case-definition")
    fun getCaseDefinitions(
        @PageableDefault(sort = ["name"], direction = Sort.Direction.ASC) pageable: Pageable
    ): ResponseEntity<Page<CaseDefinitionResponseDto>> {
        return ResponseEntity.ok(
            runWithoutAuthorization {
                service.getCaseDefinitions(pageable).map { CaseDefinitionResponseDto.of(it) }
            }
        )
    }

    @GetMapping("/management/v1/case-definition/{caseDefinitionKey}/version")
    fun getCaseDefinitionVersions(
        @LoggableResource("caseDefinitionKey") @PathVariable caseDefinitionKey: String,
    ): ResponseEntity<List<String>> {
        return ResponseEntity.ok(
            runWithoutAuthorization {
                service.getCaseDefinitionVersions(caseDefinitionKey)
            }
        )
    }

    @GetMapping("/v1/case-definition/{caseDefinitionKey}/settings")
    fun getCaseSettings(
        @LoggableResource("caseDefinitionKey") @PathVariable caseDefinitionKey: String,
    ): ResponseEntity<CaseDefinitionSettingsResponseDto> {
        return try {
            ResponseEntity.ok(
                CaseDefinitionSettingsResponseDto.of(
                    activeCaseDefinitionService.getActiveCaseDefinition(caseDefinitionKey)
                )
            )
        } catch (exception: UnknownCaseDefinitionException) {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/management/v1/case-definition/{caseDefinitionKey}/version/{caseDefinitionVersionTag}/settings")
    @RunWithoutAuthorization
    fun getCaseSettingsForManagement(
        @LoggableResource("caseDefinitionKey") @PathVariable caseDefinitionKey: String,
        @LoggableResource("caseDefinitionVersionTag") @PathVariable caseDefinitionVersionTag: String,
    ): ResponseEntity<CaseDefinitionSettingsResponseDto> {
        return try {
            ResponseEntity.ok(
                CaseDefinitionSettingsResponseDto.of(
                    service.getCaseDefinition(CaseDefinitionId.of(caseDefinitionKey, caseDefinitionVersionTag))
                )
            )
        } catch (exception: UnknownCaseDefinitionException) {
            ResponseEntity.notFound().build()
        }
    }

    @PatchMapping("/management/v1/case-definition/{caseDefinitionKey}/version/{caseDefinitionVersionTag}/settings")
    @RunWithoutAuthorization
    fun updateCaseSettingsForManagement(
        @RequestBody caseSettingsDto: CaseSettingsDto,
        @LoggableResource("caseDefinitionKey") @PathVariable caseDefinitionKey: String,
        @LoggableResource("caseDefinitionVersionTag") @PathVariable caseDefinitionVersionTag: String,
    ): ResponseEntity<CaseDefinitionSettingsResponseDto> {
        return try {
            ResponseEntity.ok(
                CaseDefinitionSettingsResponseDto.of(
                    service.updateCaseSettings(
                        CaseDefinitionId.of(caseDefinitionKey, caseDefinitionVersionTag),
                        caseSettingsDto
                    )
                )
            )
        } catch (exception: UnknownCaseDefinitionException) {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/management/v1/case-definition/{caseDefinitionKey}")
    @RunWithoutAuthorization
    fun getActive(
        @LoggableResource("caseDefinitionKey") @PathVariable caseDefinitionKey: String,
    ): ResponseEntity<CaseDefinitionResponseDto> {
        return try {
            val caseDefinition = activeCaseDefinitionService.getActiveCaseDefinition(caseDefinitionKey)
            ResponseEntity.ok(CaseDefinitionResponseDto.of(caseDefinition))
        } catch (exception: UnknownCaseDefinitionException) {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping("/management/v1/case-definition/{caseDefinitionKey}/version/{caseDefinitionVersionTag}/active")
    @RunWithoutAuthorization
    fun setActive(
        @LoggableResource("caseDefinitionKey") @PathVariable caseDefinitionKey: String,
        @LoggableResource("caseDefinitionVersionTag") @PathVariable caseDefinitionVersionTag: String,
    ): ResponseEntity<CaseDefinitionResponseDto> {
        return try {
            ResponseEntity.ok(
                CaseDefinitionResponseDto.of(
                    activeCaseDefinitionService.setGlobalActiveCaseDefinition(
                        CaseDefinitionId.of(caseDefinitionKey, caseDefinitionVersionTag)
                    )
                )
            )
        } catch (exception: UnknownCaseDefinitionException) {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/v1/case/{caseDefinitionName}/list-column")
    fun getCaseListColumn(
        @LoggableResource("documentDefinitionName") @PathVariable caseDefinitionName: String
    ): ResponseEntity<List<CaseListColumnDto>> {
        return ResponseEntity.ok().body(service.getListColumns(caseDefinitionName))
    }

    @GetMapping("/management/v1/case/{caseDefinitionName}/list-column")
    @RunWithoutAuthorization
    fun getCaseListColumnForManagement(
        @LoggableResource("documentDefinitionName") @PathVariable caseDefinitionName: String
    ): ResponseEntity<List<CaseListColumnDto>> = getCaseListColumn(caseDefinitionName)

    @PostMapping("/management/v1/case/{caseDefinitionName}/list-column")
    @RunWithoutAuthorization
    fun createCaseListColumnForManagement(
        @LoggableResource("documentDefinitionName") @PathVariable caseDefinitionName: String,
        @RequestBody caseListColumnDto: CaseListColumnDto
    ): ResponseEntity<Any> {
        service.createListColumn(caseDefinitionName, caseListColumnDto)
        return ResponseEntity.ok().build()
    }

    @PutMapping("/management/v1/case/{caseDefinitionName}/list-column")
    @RunWithoutAuthorization
    fun updateListColumnForManagement(
        @LoggableResource("documentDefinitionName") @PathVariable caseDefinitionName: String,
        @RequestBody caseListColumnDtoList: List<CaseListColumnDto>
    ): ResponseEntity<Any> {
        service.updateListColumns(caseDefinitionName, caseListColumnDtoList)
        return ResponseEntity.ok().build()
    }

    @DeleteMapping("/management/v1/case/{caseDefinitionName}/list-column/{columnKey}")
    @RunWithoutAuthorization
    fun deleteListColumnForManagement(
        @LoggableResource("documentDefinitionName") @PathVariable caseDefinitionName: String,
        @PathVariable columnKey: String
    ): ResponseEntity<Any> {
        service.deleteCaseListColumn(caseDefinitionName, columnKey)
        return ResponseEntity.noContent().build()
    }

    @GetMapping(
        "/management/v1/case/{caseDefinitionKey}/version/{caseDefinitionVersionTag}/export",
        produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE]
    )
    @RunWithoutAuthorization
    fun getExport(
        @LoggableResource("caseDefinitionKey") @PathVariable caseDefinitionKey: String,
        @LoggableResource("caseDefinitionVersionTag") @PathVariable caseDefinitionVersionTag: String,
    ): ResponseEntity<ByteArray> {
        val baos = exportService
            .export(CaseDefinitionExportRequest(CaseDefinitionId(caseDefinitionKey, caseDefinitionVersionTag)))
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm"))
        val fileName = "${caseDefinitionKey}_${caseDefinitionVersionTag}_$timestamp.valtimo.zip"
        return ResponseEntity
            .ok()
            .header("Content-Disposition", "attachment;filename=$fileName")
            .body(baos.toByteArray())
    }

    @PostMapping("/management/v1/case/import")
    @RunWithoutAuthorization
    fun import(
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<Unit> {
        return try {
            importService.import(file.inputStream, caseDefinitionRepository.findAll().map { it.id })
            ResponseEntity.ok().build()
        } catch (exception: ImportServiceException) {
            logger.info(exception) { "Import failed" }
            ResponseEntity.badRequest().build()
        }
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}
