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

package com.ritense.openzaak.service

import org.springframework.web.multipart.MultipartFile
import java.net.URI
import java.util.UUID

interface DocumentenService {

    fun createEnkelvoudigInformatieObject(documentDefinitionName: String, multipartFile: MultipartFile): URI

    fun createObjectInformatieObject(enkelvoudigInformatieObject: URI, documentId: UUID, documentDefinitionName: String)

    fun createObjectInformatieObject(enkelvoudigInformatieObject: URI, zaak: URI)
}