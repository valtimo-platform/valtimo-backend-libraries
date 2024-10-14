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

import com.ritense.authorization.annotation.RunWithoutAuthorization
import com.ritense.case.domain.CaseDefinitionSettings
import com.ritense.case.service.CaseDefinitionService
import com.ritense.case.web.rest.dto.CaseListColumnDto
import com.ritense.case.web.rest.dto.CaseSettingsDto
import com.ritense.document.exception.UnknownDocumentDefinitionException
import com.ritense.exporter.ExportService
import com.ritense.exporter.request.DocumentDefinitionExportRequest
import com.ritense.importer.ImportService
import com.ritense.importer.exception.ImportServiceException
import com.ritense.logging.LoggableResource
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
import mu.KotlinLogging
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
    private val exportService: ExportService,
    private val importService: ImportService
) {

    @GetMapping("/v1/case/{caseDefinitionName}/settings")
    fun getCaseSettings(
        @LoggableResource("documentDefinitionName") @PathVariable caseDefinitionName: String
    ): ResponseEntity<CaseDefinitionSettings> {
        return try {
            ResponseEntity.ok(
                service.getCaseSettings(caseDefinitionName)
            )
        } catch (exception: UnknownDocumentDefinitionException) {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/management/v1/case/{caseDefinitionName}/settings")
    @RunWithoutAuthorization
    fun getCaseSettingsForManagement(
        @LoggableResource("documentDefinitionName") @PathVariable caseDefinitionName: String
    ): ResponseEntity<CaseDefinitionSettings> = getCaseSettings(caseDefinitionName)

    @Deprecated("Since 11.0.0", ReplaceWith("com.ritense.case.web.rest.CaseDefinitionResource.updateCaseSettingsForManagement"))
    @PatchMapping("/v1/case/{caseDefinitionName}/settings")
    @RunWithoutAuthorization
    fun updateCaseSettings(
        @RequestBody caseSettingsDto: CaseSettingsDto,
        @LoggableResource("documentDefinitionName") @PathVariable caseDefinitionName: String
    ): ResponseEntity<CaseDefinitionSettings> = updateCaseSettingsForManagement(caseSettingsDto, caseDefinitionName)

    @PatchMapping("/management/v1/case/{caseDefinitionName}/settings")
    @RunWithoutAuthorization
    fun updateCaseSettingsForManagement(
        @RequestBody caseSettingsDto: CaseSettingsDto,
        @LoggableResource("documentDefinitionName") @PathVariable caseDefinitionName: String
    ): ResponseEntity<CaseDefinitionSettings> {
        return try {
            ResponseEntity.ok(
                service.updateCaseSettings(caseDefinitionName, caseSettingsDto)
            )
        } catch (exception: UnknownDocumentDefinitionException) {
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

    @Deprecated("Since 11.0.0", ReplaceWith("com.ritense.case.web.rest.CaseDefinitionResource.createCaseListColumnForManagement"))
    @PostMapping("/v1/case/{caseDefinitionName}/list-column")
    @RunWithoutAuthorization
    fun createCaseListColumn(
        @LoggableResource("documentDefinitionName") @PathVariable caseDefinitionName: String,
        @RequestBody caseListColumnDto: CaseListColumnDto
    ): ResponseEntity<Any> = createCaseListColumnForManagement(caseDefinitionName, caseListColumnDto)

    @PostMapping("/management/v1/case/{caseDefinitionName}/list-column")
    @RunWithoutAuthorization
    fun createCaseListColumnForManagement(
        @LoggableResource("documentDefinitionName") @PathVariable caseDefinitionName: String,
        @RequestBody caseListColumnDto: CaseListColumnDto
    ): ResponseEntity<Any> {
        service.createListColumn(caseDefinitionName, caseListColumnDto)
        return ResponseEntity.ok().build()
    }

    @Deprecated("Since 11.0.0", ReplaceWith("com.ritense.case.web.rest.CaseDefinitionResource.updateListColumnForManagement"))
    @PutMapping("/v1/case/{caseDefinitionName}/list-column")
    @RunWithoutAuthorization
    fun updateListColumn(
        @LoggableResource("documentDefinitionName") @PathVariable caseDefinitionName: String,
        @RequestBody caseListColumnDtoList: List<CaseListColumnDto>
    ): ResponseEntity<Any> = updateListColumnForManagement(caseDefinitionName, caseListColumnDtoList)

    @PutMapping("/management/v1/case/{caseDefinitionName}/list-column")
    @RunWithoutAuthorization
    fun updateListColumnForManagement(
        @LoggableResource("documentDefinitionName") @PathVariable caseDefinitionName: String,
        @RequestBody caseListColumnDtoList: List<CaseListColumnDto>
    ): ResponseEntity<Any> {
        service.updateListColumns(caseDefinitionName, caseListColumnDtoList)
        return ResponseEntity.ok().build()
    }

    @Deprecated("Since 11.0.0", ReplaceWith("com.ritense.case.web.rest.CaseDefinitionResource.deleteListColumnForManagement"))
    @DeleteMapping("/v1/case/{caseDefinitionName}/list-column/{columnKey}")
    @RunWithoutAuthorization
    fun deleteListColumn(
        @LoggableResource("documentDefinitionName") @PathVariable caseDefinitionName: String,
        @PathVariable columnKey: String
    ): ResponseEntity<Any> = deleteListColumnForManagement(caseDefinitionName, columnKey)

    @DeleteMapping("/management/v1/case/{caseDefinitionName}/list-column/{columnKey}")
    @RunWithoutAuthorization
    fun deleteListColumnForManagement(
        @LoggableResource("documentDefinitionName") @PathVariable caseDefinitionName: String,
        @PathVariable columnKey: String
    ): ResponseEntity<Any> {
        service.deleteCaseListColumn(caseDefinitionName, columnKey)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/management/v1/case/{caseDefinitionName}/{caseDefinitionVersion}/export",
        produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE]
    )
    @RunWithoutAuthorization
    fun getExport(
        @LoggableResource("documentDefinitionName") @PathVariable caseDefinitionName: String,
        @PathVariable caseDefinitionVersion: Long,
    ): ResponseEntity<ByteArray> {
        val baos = exportService
            .export(DocumentDefinitionExportRequest(caseDefinitionName, caseDefinitionVersion))
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm"))
        val fileName = "${caseDefinitionName}_${caseDefinitionVersion}_$timestamp.valtimo.zip"
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
            importService.import(file.inputStream)
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
