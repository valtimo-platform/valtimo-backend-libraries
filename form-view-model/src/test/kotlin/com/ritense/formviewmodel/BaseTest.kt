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

package com.ritense.formviewmodel

import com.ritense.form.domain.FormIoFormDefinition
import org.apache.commons.io.IOUtils
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.UUID

abstract class BaseTest {

    fun readFileAsString(fileName: String): String = this::class.java.getResource(fileName)!!.readText(Charsets.UTF_8)

    @Throws(IOException::class)
    protected fun formDefinitionOf(formDefinitionId: String): FormIoFormDefinition {
        val s = IOUtils.toString(
            Thread.currentThread().contextClassLoader.getResourceAsStream("config/form/$formDefinitionId.json"),
            StandardCharsets.UTF_8
        )
        return FormIoFormDefinition(UUID.randomUUID(), "form-example", s, false)
    }

}