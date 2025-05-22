package com.ritense.processlink.uicomponent.mapper

import com.ritense.processlink.domain.ActivityTypeWithEventName
import com.ritense.processlink.uicomponent.domain.UIComponentProcessLink
import com.ritense.processlink.uicomponent.domain.UIComponentProcessLink.Companion.TYPE_UI_COMPONENT
import com.ritense.processlink.uicomponent.dto.UIComponentProcessLinkCreateRequestDto
import com.ritense.processlink.uicomponent.dto.UIComponentProcessLinkDeployDto
import com.ritense.processlink.uicomponent.dto.UIComponentProcessLinkExportResponseDto
import com.ritense.processlink.uicomponent.dto.UIComponentProcessLinkResponseDto
import com.ritense.processlink.uicomponent.dto.UIComponentProcessLinkUpdateRequestDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

class UIComponentProcessLinkMapperTest {
    private val mapper = UIComponentProcessLinkMapper()

    @Test
    fun `should supports ProcessLinkType`() {
        mapper.supportsProcessLinkType(TYPE_UI_COMPONENT)
    }

    @Test
    fun `should map to ProcessLinkResponseDto`() {
        val processLink = createProcessLink()
        val dto = mapper.toProcessLinkResponseDto(processLink) as? UIComponentProcessLinkResponseDto

        assertThat(dto).isNotNull
        assertThat(dto!!.processLinkType).isEqualTo(TYPE_UI_COMPONENT)
        assertThat(dto.id).isEqualTo(processLink.id)
        assertThat(dto.processDefinitionId).isEqualTo(processLink.processDefinitionId)
        assertThat(dto.activityId).isEqualTo(processLink.activityId)
        assertThat(dto.activityType).isEqualTo(processLink.activityType)
        assertThat(dto.componentKey).isEqualTo(processLink.componentKey)
    }

    @Test
    fun `should map to ProcessLinkCreateRequestDto`() {
        val deployDto = createDeployDto()
        val dto = mapper.toProcessLinkCreateRequestDto(deployDto) as? UIComponentProcessLinkCreateRequestDto

        assertThat(dto).isNotNull
        assertThat(dto!!.processLinkType).isEqualTo(TYPE_UI_COMPONENT)
        assertThat(dto.processDefinitionId).isEqualTo(deployDto.processDefinitionId)
        assertThat(dto.activityId).isEqualTo(deployDto.activityId)
        assertThat(dto.activityType).isEqualTo(deployDto.activityType)
        assertThat(dto.componentKey).isEqualTo(deployDto.componentKey)
    }

    @Test
    fun `should map to ProcessLinkExportResponseDto`() {
        val processLink = createProcessLink()
        val dto = mapper.toProcessLinkExportResponseDto(processLink) as? UIComponentProcessLinkExportResponseDto

        assertThat(dto).isNotNull
        assertThat(dto!!.processLinkType).isEqualTo(TYPE_UI_COMPONENT)
        assertThat(dto.activityId).isEqualTo(processLink.activityId)
        assertThat(dto.activityType).isEqualTo(processLink.activityType)
        assertThat(dto.componentKey).isEqualTo(processLink.componentKey)
    }


    @Test
    fun `should map to new ProcessLink`() {
        val dto = createCreateDto()
        val processLink = mapper.toNewProcessLink(dto) as? UIComponentProcessLink

        assertThat(dto).isNotNull
        assertThat(processLink!!.id).isNotNull()
        assertThat(processLink.processLinkType).isEqualTo(TYPE_UI_COMPONENT)
        assertThat(processLink.processDefinitionId).isEqualTo(dto.processDefinitionId)
        assertThat(processLink.activityId).isEqualTo(dto.activityId)
        assertThat(processLink.activityType).isEqualTo(dto.activityType)
        assertThat(processLink.componentKey).isEqualTo(dto.componentKey)
    }

    private fun createCreateDto() = UIComponentProcessLinkCreateRequestDto(
        processDefinitionId = "processDefinitionId",
        activityId = "activityId",
        activityType = ActivityTypeWithEventName.USER_TASK_CREATE,
        componentKey = "componentKey",
    )

    @Test
    fun `should map to updated ProcessLink`() {
        val processLink = createProcessLink()
        val updateRequestDto = UIComponentProcessLinkUpdateRequestDto(
            id = processLink.id,
            componentKey = "updatedComponentKey",
        )
        val dto = mapper.toUpdatedProcessLink(processLink, updateRequestDto) as? UIComponentProcessLink

        assertThat(dto).isNotNull
        assertThat(dto!!.processLinkType).isEqualTo(TYPE_UI_COMPONENT)
        assertThat(dto.id).isEqualTo(processLink.id)
        assertThat(dto.processDefinitionId).isEqualTo(processLink.processDefinitionId)
        assertThat(dto.activityId).isEqualTo(processLink.activityId)
        assertThat(dto.activityType).isEqualTo(processLink.activityType)
        assertThat(dto.componentKey).isEqualTo(updateRequestDto.componentKey)
    }

    @Test
    fun `should map to updated ProcessLinkUpdateRequestDto`() {
        val existingId = UUID.randomUUID()
        val deployDto = createDeployDto()

        val dto = mapper.toProcessLinkUpdateRequestDto(deployDto, existingId) as? UIComponentProcessLinkUpdateRequestDto
        assertThat(dto).isNotNull
        assertThat(dto!!.processLinkType).isEqualTo(TYPE_UI_COMPONENT)
        assertThat(dto.id).isEqualTo(existingId)
        assertThat(dto.componentKey).isEqualTo(deployDto.componentKey)
    }

    private fun createDeployDto() = UIComponentProcessLinkDeployDto(
        processDefinitionId = "processDefinitionId",
        activityId = "activityId",
        activityType = ActivityTypeWithEventName.USER_TASK_CREATE,
        componentKey = "componentKey",
    )

    private fun createProcessLink() = UIComponentProcessLink(
        id = UUID.randomUUID(),
        processDefinitionId = "processDefinitionId",
        activityId = "activityId",
        activityType = ActivityTypeWithEventName.USER_TASK_CREATE,
        componentKey = "componentKey",
    )
}