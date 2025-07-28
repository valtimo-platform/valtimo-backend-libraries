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

package com.ritense.processlink.domain

import com.ritense.importer.ImportRequest
import com.ritense.importer.Importer

//enable the process link importer to work in tests because of the dependency on "test" mapper
class TestImporter: Importer {
    override fun type() = "test"
    override fun dependsOn() = emptySet<String>()
    override fun supports(fileName: String)= false
    override fun import(request: ImportRequest) {}
}