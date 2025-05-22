///*
// * Copyright 2015-2024 Ritense BV, the Netherlands.
// *
// * Licensed under EUPL, Version 1.2 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" basis,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package com.ritense.case.service
//
//import com.ritense.valtimo.contract.case_.CaseDefinitionId
//import org.junit.jupiter.api.Test
//import org.junit.jupiter.api.assertThrows
//import org.springframework.data.repository.findByIdOrNull
//import kotlin.test.assertEquals
//
//// TODO: Implement deploy tests
//class CaseDefinitionImporterIntTest {
//    @Test
//    fun `should deploy case definition`() {
//        val key = "test"
//        val version = "1.0.0"
//
//        caseDefinitionDeploymentService.deploy(
//            """
//            {
//                "key": "$key",
//                "versionTag": "$version",
//                "name": "Test",
//                "canHaveAssignee": true,
//                "autoAssignTasks": false
//            }
//        """.trimIndent()
//        )
//
//        val deployedCaseDefinition = caseDefinitionRepository.findByIdOrNull(CaseDefinitionId.of(key, version))
//
//        assertEquals(deployedCaseDefinition?.id?.key, key)
//        assertEquals(deployedCaseDefinition?.canHaveAssignee, true)
//        assertEquals(deployedCaseDefinition?.autoAssignTasks, false)
//    }
//
//    @Test
//    fun `should throw exception when case definition is invalid`() {
//        val invalidCaseDefinition = """
//        {
//            "key": "key",
//            "versionTag": "1.0.0",
//            "canHaveAssignee": true,
//            "autoAssignTasks": false
//        }
//    """.trimIndent()
//
//        assertThrows<IllegalArgumentException> {
//            caseDefinitionDeploymentService.deploy(invalidCaseDefinition)
//        }
//    }
//
//    @Test
//    fun `should create case settings with default values when settings are not defined`() {
//        val key = "test"
//        val version = "1.0.0"
//
//        caseDefinitionDeploymentService.deploy(
//            """
//            {
//                "key": "$key",
//                "versionTag": "$version",
//                "name": "Test"
//            }
//        """.trimIndent()
//        )
//
//        val deployedCaseDefinition = caseDefinitionRepository.findByIdOrNull(CaseDefinitionId.of(key, version))
//
//        assertEquals(deployedCaseDefinition?.id?.key, key)
//        assertEquals(deployedCaseDefinition?.canHaveAssignee, false)
//        assertEquals(deployedCaseDefinition?.autoAssignTasks, false)
//    }
//}