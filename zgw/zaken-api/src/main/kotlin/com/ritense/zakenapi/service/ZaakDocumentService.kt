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

package com.ritense.zakenapi.service

import com.ritense.catalogiapi.service.CatalogiService
import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.documentenapi.DocumentenApiPlugin
import com.ritense.documentenapi.client.DocumentInformatieObject
import com.ritense.documentenapi.domain.DocumentenApiColumnKey
import com.ritense.documentenapi.domain.DocumentenApiColumnKey.AUTEUR
import com.ritense.documentenapi.domain.DocumentenApiColumnKey.CREATIEDATUM
import com.ritense.documentenapi.domain.DocumentenApiColumnKey.INFORMATIEOBJECTTYPE_OMSCHRIJVING
import com.ritense.documentenapi.domain.DocumentenApiColumnKey.TITEL
import com.ritense.documentenapi.domain.DocumentenApiColumnKey.TREFWOORDEN
import com.ritense.documentenapi.domain.DocumentenApiColumnKey.VERTROUWELIJKHEIDAANDUIDING
import com.ritense.documentenapi.domain.DocumentenApiVersion
import com.ritense.documentenapi.service.DocumentenApiService
import com.ritense.documentenapi.service.DocumentenApiVersionService
import com.ritense.documentenapi.web.rest.dto.DocumentSearchRequest
import com.ritense.documentenapi.web.rest.dto.DocumentenApiDocumentDto
import com.ritense.documentenapi.web.rest.dto.RelatedFileDto
import com.ritense.logging.LoggableResource
import com.ritense.plugin.domain.PluginConfiguration
import com.ritense.plugin.service.PluginService
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.zakenapi.ZaakUrlProvider
import com.ritense.zakenapi.ZakenApiPlugin
import com.ritense.zakenapi.domain.ZaakInformatieObject
import com.ritense.zakenapi.domain.ZaakResponse
import com.ritense.zakenapi.link.ZaakInstanceLinkNotFoundException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.net.URI
import java.util.UUID
import kotlin.math.min

@Transactional
@Service
@SkipComponentScan
class ZaakDocumentService(
    private val zaakUrlProvider: ZaakUrlProvider,
    private val pluginService: PluginService,
    private val catalogiService: CatalogiService,
    private val documentenApiService: DocumentenApiService,
    private val documentenApiVersionService: DocumentenApiVersionService,
) {

    fun getInformatieObjectenAsRelatedFiles(
        @LoggableResource(resourceType = JsonSchemaDocument::class) documentId: UUID
    ): List<RelatedFileDto> {
        val zaakUri = zaakUrlProvider.getZaakUrl(documentId)

        val zakenApiPlugin = checkNotNull(
            pluginService.createInstance(
                ZakenApiPlugin::class.java,
                ZakenApiPlugin.findConfigurationByUrl(zaakUri)
            )
        ) { "Could not find ${ZakenApiPlugin::class.simpleName} configuration for zaak with url: $zaakUri" }

        return zakenApiPlugin.getZaakInformatieObjecten(zaakUri)
            .map { getRelatedFiles(it) }
    }

    fun getInformatieObjectenAsRelatedFilesPage(
        @LoggableResource(resourceType = JsonSchemaDocument::class) documentId: UUID,
        documentSearchRequest: DocumentSearchRequest,
        pageable: Pageable,
    ): Page<DocumentenApiDocumentDto> {
        val zaakUri = zaakUrlProvider.getZaakUrl(documentId)
        val version = documentenApiVersionService.getVersionByDocumentId(documentId)
        check(documentSearchRequest.informatieobjecttype != null, INFORMATIEOBJECTTYPE_OMSCHRIJVING, version)
        check(documentSearchRequest.titel != null, TITEL, version)
        check(documentSearchRequest.vertrouwelijkheidaanduiding != null, VERTROUWELIJKHEIDAANDUIDING, version)
        check(documentSearchRequest.creatiedatumFrom != null, CREATIEDATUM, version)
        check(documentSearchRequest.creatiedatumTo != null, CREATIEDATUM, version)
        check(documentSearchRequest.auteur != null, AUTEUR, version)
        check(documentSearchRequest.trefwoorden != null, TREFWOORDEN, version)
        if (!version.supportsTrefwoorden) {
            require(documentSearchRequest.trefwoorden == null) {
                "Unsupported field 'trefwoorden' on Documenten API version '$version'"
            }
        }
        return if (version.supportsFilterableColumns() && version.supportsSortableColumns()) {
            documentenApiService.getCaseInformatieObjecten(
                documentId,
                documentSearchRequest.copy(zaakUrl = zaakUri),
                pageable
            ).map { mapDocumentenApiDocument(it, version) }
        } else {
            val zakenApiPlugin = getZakenApiPlugin(zaakUri)
            val documenten = zakenApiPlugin.getZaakInformatieObjecten(zaakUri)
                .map { mapDocumentenApiDocument(it, version) }
            return toPage(documenten, pageable)
        }
    }

    private fun check(shouldCheck: Boolean, columnKey: DocumentenApiColumnKey, version: DocumentenApiVersion) {
        if (shouldCheck) {
            val fieldName = columnKey.property
            check(version.filterableColumns.contains(fieldName)) {
                "Unsupported filter '$fieldName' on Documenten API with version $version"
            }
        }
    }

    private fun <T> toPage(list: List<T>, pageable: Pageable): Page<T> {
        val startIndex = pageable.offset.toInt()
        val endIndex = min(startIndex + pageable.pageSize, list.size)
        return PageImpl(list.subList(startIndex, endIndex), pageable, list.size.toLong())
    }

    private fun getRelatedFiles(zaakInformatieObject: ZaakInformatieObject): RelatedFileDto {
        val pluginConfiguration = getDocumentenApiPluginByInformatieobjectUrl(zaakInformatieObject.informatieobject)
        val plugin = pluginService.createInstance(pluginConfiguration) as DocumentenApiPlugin
        val informatieObject = plugin.getInformatieObject(zaakInformatieObject.informatieobject)
        return mapRelatedFile(informatieObject, pluginConfiguration)
    }

    private fun mapRelatedFile(
        informatieObject: DocumentInformatieObject,
        pluginConfiguration: PluginConfiguration
    ): RelatedFileDto {
        return RelatedFileDto(
            fileId = UUID.fromString(informatieObject.url.path.substringAfterLast("/")),
            fileName = informatieObject.bestandsnaam,
            sizeInBytes = informatieObject.bestandsomvang,
            createdOn = informatieObject.creatiedatum.atStartOfDay(),
            createdBy = informatieObject.auteur,
            author = informatieObject.auteur,
            title = informatieObject.titel,
            status = informatieObject.status?.key,
            language = informatieObject.taal,
            pluginConfigurationId = pluginConfiguration.id.id,
            identification = informatieObject.identificatie,
            description = informatieObject.beschrijving,
            informatieobjecttype = informatieObject.informatieobjecttype,
            informatieobjecttypeOmschrijving = getInformatieobjecttypeOmschrijvingByUri(informatieObject.informatieobjecttype),
            keywords = informatieObject.trefwoorden,
            format = informatieObject.formaat,
            sendDate = informatieObject.verzenddatum,
            receiptDate = informatieObject.ontvangstdatum,
            confidentialityLevel = informatieObject.vertrouwelijkheidaanduiding?.key,
            version = informatieObject.versie,
            indicationUsageRights = informatieObject.indicatieGebruiksrecht
        )
    }

    private fun mapDocumentenApiDocument(
        informatieObject: DocumentInformatieObject,
        version: DocumentenApiVersion,
    ): DocumentenApiDocumentDto {
        val pluginConfiguration = getDocumentenApiPluginByInformatieobjectUrl(informatieObject.url)
        val trefwoorden = if (version.supportsTrefwoorden) {
            informatieObject.trefwoorden
        } else {
            listOf()
        }
        return DocumentenApiDocumentDto(
            fileId = UUID.fromString(informatieObject.url.path.substringAfterLast("/")),
            pluginConfigurationId = pluginConfiguration.id.id,
            bestandsnaam = informatieObject.bestandsnaam,
            bestandsomvang = informatieObject.bestandsomvang,
            creatiedatum = informatieObject.creatiedatum.atStartOfDay(),
            auteur = informatieObject.auteur,
            titel = informatieObject.titel,
            status = informatieObject.status?.key,
            taal = informatieObject.taal,
            identificatie = informatieObject.identificatie,
            beschrijving = informatieObject.beschrijving,
            informatieobjecttype = informatieObject.informatieobjecttype,
            informatieobjecttypeOmschrijving = getInformatieobjecttypeOmschrijvingByUri(informatieObject.informatieobjecttype),
            trefwoorden = trefwoorden,
            formaat = informatieObject.formaat,
            verzenddatum = informatieObject.verzenddatum,
            ontvangstdatum = informatieObject.ontvangstdatum,
            vertrouwelijkheidaanduiding = informatieObject.vertrouwelijkheidaanduiding?.key,
            versie = informatieObject.versie,
            indicatieGebruiksrecht = informatieObject.indicatieGebruiksrecht
        )
    }

    private fun mapDocumentenApiDocument(
        zaakInformatieObject: ZaakInformatieObject,
        version: DocumentenApiVersion,
    ): DocumentenApiDocumentDto {
        val pluginConfiguration = getDocumentenApiPluginByInformatieobjectUrl(zaakInformatieObject.informatieobject)
        val plugin = pluginService.createInstance(pluginConfiguration) as DocumentenApiPlugin
        val informatieObject = plugin.getInformatieObject(zaakInformatieObject.informatieobject)
        val trefwoorden = if (version.supportsTrefwoorden) {
            informatieObject.trefwoorden
        } else {
            listOf()
        }
        return DocumentenApiDocumentDto(
            fileId = UUID.fromString(informatieObject.url.path.substringAfterLast("/")),
            pluginConfigurationId = pluginConfiguration.id.id,
            bestandsnaam = informatieObject.bestandsnaam,
            bestandsomvang = informatieObject.bestandsomvang,
            creatiedatum = informatieObject.creatiedatum.atStartOfDay(),
            auteur = informatieObject.auteur,
            titel = informatieObject.titel,
            status = informatieObject.status?.key,
            taal = informatieObject.taal,
            identificatie = informatieObject.identificatie,
            beschrijving = informatieObject.beschrijving,
            informatieobjecttype = informatieObject.informatieobjecttype,
            informatieobjecttypeOmschrijving = getInformatieobjecttypeOmschrijvingByUri(informatieObject.informatieobjecttype),
            trefwoorden = trefwoorden,
            formaat = informatieObject.formaat,
            verzenddatum = informatieObject.verzenddatum,
            ontvangstdatum = informatieObject.ontvangstdatum,
            vertrouwelijkheidaanduiding = informatieObject.vertrouwelijkheidaanduiding?.key,
            versie = informatieObject.versie,
            indicatieGebruiksrecht = informatieObject.indicatieGebruiksrecht
        )
    }

    private fun getInformatieobjecttypeOmschrijvingByUri(uri: String?): String? {
        return uri?.let { catalogiService.getInformatieobjecttype(URI(it))?.omschrijving }
    }

    private fun getDocumentenApiPluginByInformatieobjectUrl(informatieobjectUrl: URI): PluginConfiguration {
        return checkNotNull(
            pluginService.findPluginConfiguration(
                DocumentenApiPlugin::class.java,
                DocumentenApiPlugin.findConfigurationByUrl(informatieobjectUrl)
            )
        ) { "Could not find ${DocumentenApiPlugin::class.simpleName} configuration for informatieobjectUrl: $informatieobjectUrl" }

    }

    fun getZaakByDocumentId(
        @LoggableResource(resourceType = JsonSchemaDocument::class) documentId: UUID
    ): ZaakResponse? {
        val url = try {
            zaakUrlProvider.getZaakUrl(documentId)
        } catch (e: ZaakInstanceLinkNotFoundException) {
            return null
        }
        val plugin = pluginService.createInstance(
            ZakenApiPlugin::class.java,
            ZakenApiPlugin.findConfigurationByUrl(url)
        )

        return plugin?.getZaak(url)
    }

    fun getZaakByDocumentIdOrThrow(
        @LoggableResource(resourceType = JsonSchemaDocument::class) documentId: UUID
    ): ZaakResponse {
        val url = zaakUrlProvider.getZaakUrl(documentId)
        val plugin = pluginService.createInstance(
            ZakenApiPlugin::class.java,
            ZakenApiPlugin.findConfigurationByUrl(url)
        )
            ?: throw IllegalStateException("Missing plugin configuration of type '${ZakenApiPlugin.PLUGIN_KEY}' for url '$url'")
        return plugin.getZaak(url)
    }

    private fun getZakenApiPlugin(zaakUri: URI): ZakenApiPlugin {
        return checkNotNull(
            pluginService.createInstance(
                ZakenApiPlugin::class.java,
                ZakenApiPlugin.findConfigurationByUrl(zaakUri)
            )
        ) { "Could not find ${ZakenApiPlugin::class.simpleName} configuration for zaak with url: $zaakUri" }
    }

}