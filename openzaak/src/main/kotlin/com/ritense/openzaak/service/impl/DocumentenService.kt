/*
 * Copyright 2015-2020 Ritense BV, the Netherlands.
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

package com.ritense.openzaak.service.impl

import com.ritense.openzaak.service.DocumentenService
import com.ritense.openzaak.service.impl.model.documenten.DocumentCreatedResult
import com.ritense.openzaak.service.impl.model.documenten.ZaakInformatieObjectCreatedResult
import com.ritense.valtimo.contract.utils.SecurityUtils
import org.springframework.web.client.RestTemplate
import org.springframework.web.multipart.MultipartFile
import java.net.URI
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Base64
import java.util.UUID

class DocumentenService(
    private val restTemplate: RestTemplate,
    private val openZaakConfigService: OpenZaakConfigService,
    private val openZaakTokenGeneratorService: OpenZaakTokenGeneratorService,
    private val informatieObjectTypeLinkService: InformatieObjectTypeLinkService,
    private val zaakTypeLinkService: ZaakTypeLinkService
) : DocumentenService {

    override fun createEnkelvoudigInformatieObject(documentDefinitionName: String, multipartFile: MultipartFile): URI {
        val informatieObjectTypeLink = informatieObjectTypeLinkService.get(documentDefinitionName)!!

        return OpenZaakRequestBuilder(restTemplate, openZaakConfigService, openZaakTokenGeneratorService)
            .path("/documenten/api/v1/enkelvoudiginformatieobjecten")
            .post()
            .body(
                mapOf(
                    "bronorganisatie" to openZaakConfigService.get()!!.rsin.value,
                    "creatiedatum" to LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    "titel" to multipartFile.originalFilename,
                    "auteur" to SecurityUtils.getCurrentUserLogin(),
                    "bestandsnaam" to multipartFile.originalFilename,
                    "taal" to "nld",
                    "inhoud" to Base64.getEncoder().encodeToString(multipartFile.bytes),
                    "informatieobjecttype" to informatieObjectTypeLink.informatieObjectType
                )
            )
            .build()
            .execute(DocumentCreatedResult::class.java).url
    }

    override fun createObjectInformatieObject(enkelvoudigInformatieObject: URI, documentId: UUID, documentDefinitionName: String) {
        val zaakInstance = zaakTypeLinkService.findBy(documentDefinitionName).getZaakInstanceLink(documentId).zaakInstanceUrl

        OpenZaakRequestBuilder(restTemplate, openZaakConfigService, openZaakTokenGeneratorService)
            .path("/zaken/api/v1/zaakinformatieobjecten")
            .post()
            .body(
                mapOf(
                    "informatieobject" to enkelvoudigInformatieObject,
                    "zaak" to zaakInstance,
                )
            )
            .build()
            .execute(ZaakInformatieObjectCreatedResult::class.java)
    }

}