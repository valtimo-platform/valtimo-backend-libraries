/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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

package com.ritense.import

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.never
import org.mockito.kotlin.spy
import org.mockito.kotlin.times

class ValtimoImportServiceTest {

    @Test
    fun `should not instantiate on duplicate importer types`() {
        val exception = assertThrows<ImportServiceException> {
            ValtimoImportService(
                setOf(
                    TestImporter(),
                    TestImporter()
                )
            )
        }

        assertThat(exception.message)
            .isEqualTo("Multiple importers of the same type provided: [test]")
    }

    @Test
    fun `should not instantiate on cyclic importer dependencies`() {
        val exception = assertThrows<ImportServiceException> {
            ValtimoImportService(
                linkedSetOf(
                    TestImporter("2", dependsOn = setOf("1","3")),
                    TestImporter("1"),
                    TestImporter("3", dependsOn = setOf("2"))
                )
            )
        }

        assertThat(exception.message)
            .isEqualTo("Importer dependencies could not be resolved or contain a cyclic reference! Error occurred after: [1]")
    }

    @Test
    fun `should import files from zip in the right order`() {
        val fileCount = 5

        val importers = IntRange(1, fileCount)
            .map { index ->
                val dependsOn: Set<String> = if (index <= 1) setOf() else setOf("${index - 1}")
                val value = index.toString()

                spy(TestImporter(value, dependsOn,
                    { it == value },
                    { check(it.content.toString(Charsets.UTF_8) == value) }
                ))
            }

        val importService = ValtimoImportService(importers.shuffled().toSet())

        val skip = 3
        val inputStream = createZipInputStream(fileCount, skip)
        importService.import(inputStream)

        // Verify the importers are called in the correct order
        val inOrder = inOrder(*importers.toTypedArray())
        importers.forEach {
            val verificationMode = if (it.type() == skip.toString()) never() else times(1)
            inOrder.verify(it, verificationMode).import(any())
        }
    }

    private fun createZipInputStream(size: Int = 5, skip: Int? = null): InputStream {
        val outputStream = ByteArrayOutputStream()
        ZipOutputStream(outputStream).use { zipStream ->
            // Start at 0 to add a file that has no candidate importer
            IntRange(0, size).shuffled()
                .filterNot { it == skip }
                .map { it.toString() }
                .forEach { index ->
                    zipStream.putNextEntry(ZipEntry(index))
                    zipStream.write(index.toByteArray())
                    zipStream.closeEntry()
                }
        }

        return ByteArrayInputStream(outputStream.toByteArray())
    }
}