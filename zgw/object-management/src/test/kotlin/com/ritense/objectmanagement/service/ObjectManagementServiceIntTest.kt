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
        val objectManagement = objectManagementService.createAndUpdate(ObjectManagement(
            title = "test",
            objectenApiPluginConfigurationId = UUID.randomUUID(),
            objecttypeId = UUID.randomUUID(),
            objecttypenApiPluginConfigurationId = UUID.randomUUID()
        ))
        assertThat(objectManagement).isNotNull
    }

    @Test
    @Order(2)
    fun getById() {
        val objectManagement = objectManagementService.createAndUpdate(ObjectManagement(
            title = "test1",
            objectenApiPluginConfigurationId = UUID.randomUUID(),
            objecttypeId = UUID.randomUUID(),
            objecttypenApiPluginConfigurationId = UUID.randomUUID()
        ))
        val toReviewObjectManagement = objectManagementService.getById(objectManagement.id)
        assertThat(objectManagement.id).isEqualTo(toReviewObjectManagement?.id)
        assertThat(objectManagement.title).isEqualTo(toReviewObjectManagement?.title)
        assertThat(objectManagement.objecttypenApiPluginConfigurationId)
            .isEqualTo(toReviewObjectManagement?.objecttypenApiPluginConfigurationId)


    }

    @Test
    @Order(2)
    fun getAll() {
        objectManagementService.createAndUpdate(ObjectManagement(
            title = "test2",
            objectenApiPluginConfigurationId = UUID.randomUUID(),
            objecttypeId = UUID.randomUUID(),
            objecttypenApiPluginConfigurationId = UUID.randomUUID()
        ))
        objectManagementService.createAndUpdate(ObjectManagement(
            title = "test3",
            objectenApiPluginConfigurationId = UUID.randomUUID(),
            objecttypeId = UUID.randomUUID(),
            objecttypenApiPluginConfigurationId = UUID.randomUUID()
        ))
        val objectManagementList = objectManagementService.getAll()
        assertThat(objectManagementList.size).isEqualTo(2)
    }
}