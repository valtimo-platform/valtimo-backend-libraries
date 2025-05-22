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

package com.ritense.valtimo.contract.repository

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.semver4j.Semver

class SemverConverterTest {

    @Test
    fun `convert db string to semver`() {
        val converter = SemverConverter()

        val semver = converter.convertToEntityAttribute("000001.000000.000000")
        assertEquals(Semver.of(1,0,0), semver)
    }

    @Test
    fun `convert semver to db string`() {
        val converter = SemverConverter()

        val semverString = converter.convertToDatabaseColumn(Semver.of(1,23,456))
        assertEquals("000001.000023.000456", semverString)
    }

    @Test
    fun `convert semver with prelease version to db string`() {
        val converter = SemverConverter()
        val version = Semver.Builder()
            .withMajor(1)
            .withMinor(23)
            .withPatch(456)
            .withPreReleases (arrayOf("alpha", "2"))
            .withBuilds(arrayOf("build", "4"))
            .toSemver()

        val semverString = converter.convertToDatabaseColumn(version)
        assertEquals("000001.000023.000456-alpha.2+build.4", semverString)
    }

    @Test
    fun `convert db string with prelease version to semver`() {
        val converter = SemverConverter()
        val version = Semver.Builder()
            .withMajor(1)
            .withMinor(23)
            .withPatch(456)
            .withPreReleases (arrayOf("alpha", "2"))
            .withBuilds(arrayOf("build", "4"))
            .toSemver()

        val semver = converter.convertToEntityAttribute("000001.000023.000456-alpha.2+build.4")
        assertEquals(version, semver)
    }
}