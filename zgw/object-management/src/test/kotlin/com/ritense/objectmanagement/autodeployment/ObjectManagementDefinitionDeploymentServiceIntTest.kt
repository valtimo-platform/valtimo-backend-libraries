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

package com.ritense.objectmanagement.autodeployment

import com.ritense.objectmanagement.BaseIntegrationTest
import com.ritense.objectmanagement.service.ObjectManagementService
import javax.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired


@Transactional
internal class ObjectManagementDefinitionDeploymentServiceIntTest: BaseIntegrationTest() {

    @Autowired
    lateinit var objectManagementService: ObjectManagementService

    @Test
    fun getById() {
        val objectManagement = objectManagementService.findByObjectTypeId("4416cbef-dda3-41f4-bf5c-633f7fe14847")
        assertThat(objectManagement).isNotNull
        assertThat(objectManagement?.title).isEqualTo("My Object Management")
    }
}