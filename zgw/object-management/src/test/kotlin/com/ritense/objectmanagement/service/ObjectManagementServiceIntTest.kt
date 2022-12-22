package com.ritense.objectmanagement.service

import com.ritense.objectmanagement.BaseIntegrationTest
import com.ritense.objectmanagement.domain.ObjectManagement
import java.util.UUID
import javax.inject.Inject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ObjectManagementServiceIntTest: BaseIntegrationTest() {

    @Inject
    lateinit var objectManagementService: ObjectManagementService

    @Test
    fun `objectManagementConfiguration can be created`() {
        val objectManagement = objectManagementService.createAndUpdate(ObjectManagement(
            title = "test",
            objectenApiPluginConfigurationId = UUID.randomUUID().toString(),
            objecttypeId = UUID.randomUUID(),
            objecttypenApiPluginConfigurationId = UUID.randomUUID()
        ))
        assertThat(objectManagement).isNotNull
    }

    @Test
    fun getById() {
        val objectManagement = objectManagementService.createAndUpdate(ObjectManagement(
            title = "test1",
            objectenApiPluginConfigurationId = UUID.randomUUID().toString(),
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
    fun getAll() {
        val objectManagementList = objectManagementService.getAll()
        assertThat(objectManagementList.size).isEqualTo(2)
    }
}