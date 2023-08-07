/*
 *  Copyright 2015-2023 Ritense BV, the Netherlands.
 *
 *  Licensed under EUPL, Version 1.2 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.ritense.valtimo.changelog

import com.ritense.valtimo.BaseIntegrationTest
import com.ritense.valtimo.changelog.repository.ChangesetRepository
import com.ritense.valtimo.changelog.service.ChangelogDeployer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import java.time.Instant

internal class ChangelogDeployerIntTest : BaseIntegrationTest() {

    @Autowired
    lateinit var changesetRepository: ChangesetRepository

    @Autowired
    lateinit var changelogDeployer: ChangelogDeployer

    @Autowired
    lateinit var testTypeChangesetDeployer: TestTypeChangesetDeployer

    @Test
    fun `should deploy  file from resource folder`() {

        val changeset = changesetRepository.findById("initial-testtype")

        assertThat(changeset.isPresent).isTrue()
        assertThat(changeset.get().filename).endsWith("/initial.testtype.json")
        assertThat(changeset.get().dateExecuted).isBetween(Instant.parse("2023-06-13T00:00:00Z"), Instant.now())
        assertThat(changeset.get().orderExecuted).isBetween(0, 1000)
        assertThat(changeset.get().md5sum).isEqualTo("c29a5747d698b2f95cdfd5ed6502f19d")
    }

    @Test
    fun `should ignore whitespace changes in changeset`() {
        val filename = "/initial.testtype.json"
        val content = """
            {"testContent":    ["a",     "b" ,
                 "c"
            ] , "changesetId" :   "initial-testtype"}"""

        changelogDeployer.deploy(testTypeChangesetDeployer, filename, content)

        val changeset = changesetRepository.findById("initial-testtype")
        assertThat(changeset.isPresent).isTrue()
    }

    @Test
    fun `should throw error when changeset changed`() {
        val filename = "/initial.testtype.json"
        val content = """
            {
                "changesetId": "initial-testtype",
                "testContent": ["a", "b", "c", "new-value"]
            }"""

        val exception = assertThrows<RuntimeException> {
            changelogDeployer.deploy(testTypeChangesetDeployer, filename, content)
        }
        assertThat(exception.message).isEqualTo("Computed checksum '990719bf4ba2ec171091f913ccc6164c' doesn't match existing 'c29a5747d698b2f95cdfd5ed6502f19d' for test/config/import/initial.testtype.json")
    }
}
