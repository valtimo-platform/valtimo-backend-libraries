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

package com.ritense.documentgeneration.service

import com.ritense.documentgeneration.domain.GeneratedDocument
import com.ritense.documentgeneration.domain.placeholders.TemplatePlaceholders
import com.ritense.documentgeneration.domain.templatedata.TemplateData
import org.springframework.http.MediaType

class LocalPdfDocumentGenerator : PdfDocumentGenerator {

    override fun getTemplatePlaceholders(templateName: String?): TemplatePlaceholders {
        throw NotImplementedError()
    }

    override fun generateDocument(templateName: String?, templateData: TemplateData?): GeneratedDocument {
        throw NotImplementedError()
    }

    override fun getDocumentMediaType(): MediaType {
        throw NotImplementedError()
    }

    companion object {
        private const val NOT_YET_IMPLEMENTED_MSG = "Not yet implemented"
    }
}