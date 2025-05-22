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

package com.ritense.case.service

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.case.BaseIntegrationTest
import com.ritense.case.domain.CaseTabType
import com.ritense.case.service.exception.TabAlreadyExistsException
import com.ritense.case.web.rest.dto.CaseTabDto
import com.ritense.document.service.DocumentDefinitionService
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

class CaseTabServiceIntTest @Autowired constructor(
    private val caseTabService: CaseTabService,
    private val documentDefinitionService: DocumentDefinitionService
) : BaseIntegrationTest() {

    @Test
    @Transactional
    fun `should create new tab`() {

        val caseTab = runWithoutAuthorization {
            val caseDefinitionName = "some-case-type"

            documentDefinitionService.deploy(
                """
                {
                    "${'$'}id": "$caseDefinitionName.schema",
                    "${'$'}schema": "http://json-schema.org/draft-07/schema#"
                }
            """.trimIndent()
            )

            val dto = CaseTabDto(
                key = "some-key",
                name = "Some name",
                type = CaseTabType.STANDARD,
                contentKey = "some-content-key",
                showTasks = true
            )

            caseTabService.createCaseTab(
                caseDefinitionName,
                dto
            )
        }

        Assertions.assertThat(caseTab).isNotNull
    }

    @Test
    @Transactional
    fun `should fail creating new tab with existing key`() {

        val caseTab = runWithoutAuthorization {
            val caseDefinitionName = "some-case-type"

            documentDefinitionService.deploy(
                """
                {
                    "${'$'}id": "$caseDefinitionName.schema",
                    "${'$'}schema": "http://json-schema.org/draft-07/schema#"
                }
            """.trimIndent()
            )

            val dto = CaseTabDto(
                key = "some-key",
                name = "Some name",
                type = CaseTabType.STANDARD,
                contentKey = "some-content-key",
                showTasks = true
            )

            caseTabService.createCaseTab(
                caseDefinitionName,
                dto
            )

            assertThrows<TabAlreadyExistsException> {
                caseTabService.createCaseTab(
                    caseDefinitionName,
                    dto
                )
            }
        }

        Assertions.assertThat(caseTab).isNotNull
    }

    @Test
    @Transactional
    fun `should not create new tab due to validation errors`() {

        val exception = assertThrows<IllegalArgumentException> {
            runWithoutAuthorization {
                val caseDefinitionName = "some-case-type"

                documentDefinitionService.deploy(
                    """
                {
                    "${'$'}id": "$caseDefinitionName.schema",
                    "${'$'}schema": "http://json-schema.org/draft-07/schema#"
                }
            """.trimIndent()
                )

                val dto = CaseTabDto(
                    key = "",
                    name = "",
                    type = CaseTabType.STANDARD,
                    contentKey = "",
                    showTasks = true
                )

                caseTabService.createCaseTab(
                    caseDefinitionName,
                    dto
                )
            }
        }

        Assertions.assertThat(exception.message).isEqualTo("key was blank!")
    }

    @Test
    @Transactional
    fun `should not create new tab when case definition does not exist`() {

        val caseDefinitionName = "some-case-type-that-does-not-exist"

        val dto = CaseTabDto(
            key = "some-key",
            name = "Some name",
            type = CaseTabType.STANDARD,
            contentKey = "some-content-key",
            showTasks = true
        )

        val exception = assertThrows<NoSuchElementException> {
            runWithoutAuthorization {
                caseTabService.createCaseTab(
                    caseDefinitionName,
                    dto
                )
            }
        }

        Assertions.assertThat(exception.message).isEqualTo("Case definition with name $caseDefinitionName does not exist!")
    }
}