/*
 * Copyright 2015-2025 Ritense BV, the Netherlands.
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

package com.ritense

import com.ritense.importer.ImportRequest
import com.ritense.importer.Importer
import com.ritense.importer.ValtimoImportTypes.Companion.CASE_DEFINITION
import com.ritense.importer.ValtimoImportTypes.Companion.FORM
import com.ritense.importer.ValtimoImportTypes.Companion.PROCESS_DEFINITION
import org.springframework.transaction.annotation.Transactional

@Transactional
class FakeFormDefinitionImporter(
) : Importer {
    override fun type(): String = FORM

    override fun dependsOn(): Set<String> = setOf(CASE_DEFINITION, PROCESS_DEFINITION)

    override fun supports(fileName: String): Boolean = false

    override fun import(request: ImportRequest) {
    }
}