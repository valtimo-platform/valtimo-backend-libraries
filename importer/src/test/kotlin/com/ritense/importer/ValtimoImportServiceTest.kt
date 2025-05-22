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

package com.ritense.importer

import com.ritense.importer.exception.CyclicImporterDependencyException
import com.ritense.importer.exception.DuplicateImporterTypeException
import com.ritense.importer.exception.InvalidImportZipException
import com.ritense.importer.exception.TooManyImportCandidatesException
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.never
import org.mockito.kotlin.spy
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

class ValtimoImportServiceTest {

    @Test
    fun `should not instantiate on duplicate importer types`() {
        val exception = assertThrows<DuplicateImporterTypeException> {
            ValtimoImportService(
                setOf(
                    TestImporter(),
                    TestImporter()
                )
            )
        }

        assertThat(exception.duplicatedTypes)
            .isEqualTo(setOf("test"))
    }

    @Test
    fun `should filter importer types that depend on unprovided importer`() {
        val filteredImporter = spy(TestImporter(type = "other", dependsOn = setOf("not-provided")))
        val service = ValtimoImportService(
            // Create two importers that both accept the provided file
            setOf(
                filteredImporter
            )
        )

        //Should not throw TooManyImportCandidatesException, since 'other' is filtered out
        service.import(createZipInputStream(1))

        verify(filteredImporter, never()).supports(any())
    }

    @Test
    fun `should throw TooManyImportCandidatesException`() {
        val exception = assertThrows<TooManyImportCandidatesException> {
            val service = ValtimoImportService(
                // Create two importers that both accept the provided file
                setOf(
                    TestImporter("test1"),
                    TestImporter("test2"),
                    TestImporter("test3", supportsFunction = { false })
                )
            )

            service.import(createZipInputStream(1))
        }

        assertThat(exception.importerTypes).isEqualTo(setOf("test1", "test2"))
    }

    @Test
    fun `should not instantiate on cyclic importer dependencies`() {
        val exception = assertThrows<CyclicImporterDependencyException> {
            ValtimoImportService(
                linkedSetOf(
                    TestImporter("2", dependsOn = setOf("2")),
                    TestImporter("1"),
                )
            )
        }

        assertThat(exception.afterImporterTypes)
            .isEqualTo(setOf("1"))
    }

    @Test
    fun `should throw InvalidImportZipException`() {
        val importService = ValtimoImportService(setOf())
        assertThrows<InvalidImportZipException> {
            importService.import("123456".byteInputStream(Charsets.UTF_8))
        }
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

        // do no create file "3"
        val skip = 3
        val inputStream = createZipInputStream(fileCount, skip)
        importService.import(inputStream)

        // Verify the importers are called in the correct order
        val inOrder = inOrder(*importers.toTypedArray())
        importers.forEach { importer ->
            val verificationMode = if (importer.type() == skip.toString()) never() else times(1)
            val importRequestCaptor = argumentCaptor<ImportRequest>()
            inOrder.verify(importer, verificationMode).import(importRequestCaptor.capture())

            val importRequest = importRequestCaptor.allValues.firstOrNull()
            if (importRequest != null) {
                assertThat(importRequest.fileName).isNotBlank()
                assertThat(importer.supports(importRequest.fileName)).isTrue()
            }
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