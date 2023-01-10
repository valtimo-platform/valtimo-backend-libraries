/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

package com.ritense.objectmanagement.service

import com.ritense.objectmanagement.BaseIntegrationTest
import com.ritense.objectmanagement.domain.ObjectManagement
import java.util.UUID
import javax.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@Transactional
internal class ObjectManagementServiceIntTest: BaseIntegrationTest() {

    @Autowired
    lateinit var objectManagementService: ObjectManagementService

    @Test
    @Order(1)
    fun `objectManagementConfiguration can be created`() {
        val objectManagement = createObjectManagement()
        assertThat(objectManagement).isNotNull
    }

    @Test
    @Order(2)
    fun getById() {
        val objectManagement = createObjectManagement()
        val toReviewObjectManagement = objectManagementService.getById(objectManagement.id)
        assertThat(objectManagement.id).isEqualTo(toReviewObjectManagement?.id)
        assertThat(objectManagement.title).isEqualTo(toReviewObjectManagement?.title)
        assertThat(objectManagement.objecttypenApiPluginConfigurationId)
            .isEqualTo(toReviewObjectManagement?.objecttypenApiPluginConfigurationId)
    }

    @Test
    @Order(2)
    fun getAll() {
        createObjectManagement("test1")
        createObjectManagement("test2")

        val objectManagementList = objectManagementService.getAll()
        assertThat(objectManagementList.size).isEqualTo(2)
    }

    @Test
    @Order(2)
    fun deleteById() {
       val objectManagement = createObjectManagement()

        objectManagementService.deleteById(objectManagement.id)

        val objectManagementList = objectManagementService.getAll()
        assertThat(!objectManagementList.contains(objectManagement))
    }

    private fun createObjectManagement(title: String? = null): ObjectManagement =
        objectManagementService.create(ObjectManagement(
            title = title ?: "test",
            objectenApiPluginConfigurationId = UUID.randomUUID(),
            objecttypeId = UUID.randomUUID().toString(),
            objecttypenApiPluginConfigurationId = UUID.randomUUID()
        ))
}