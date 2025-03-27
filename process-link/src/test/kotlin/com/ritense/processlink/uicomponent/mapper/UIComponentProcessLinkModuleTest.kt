package com.ritense.processlink.uicomponent.mapper

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath
import com.ritense.processlink.autodeployment.ProcessLinkDeployDto
import com.ritense.processlink.domain.ActivityTypeWithEventName
import com.ritense.processlink.uicomponent.domain.UIComponentProcessLink.Companion.TYPE_UI_COMPONENT
import com.ritense.processlink.uicomponent.dto.UIComponentProcessLinkCreateRequestDto
import com.ritense.processlink.uicomponent.dto.UIComponentProcessLinkDeployDto
import com.ritense.processlink.uicomponent.dto.UIComponentProcessLinkExportResponseDto
import com.ritense.processlink.uicomponent.dto.UIComponentProcessLinkResponseDto
import com.ritense.processlink.uicomponent.dto.UIComponentProcessLinkUpdateRequestDto
import com.ritense.processlink.web.rest.dto.ProcessLinkCreateRequestDto
import com.ritense.processlink.web.rest.dto.ProcessLinkExportResponseDto
import com.ritense.processlink.web.rest.dto.ProcessLinkUpdateRequestDto
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import java.util.UUID


class UIComponentProcessLinkModuleTest {
    private val objectMapper = jacksonObjectMapper().registerModules(UIComponentProcessLinkModule())

    @Test
    fun `should deserialize UIComponentProcessLinkCreateRequestDto`() {
        val json = objectMapper.writeValueAsString(
            UIComponentProcessLinkCreateRequestDto(
                processDefinitionId = "processDefinitionId",
                activityId = "activityId",
                activityType = ActivityTypeWithEventName.USER_TASK_CREATE,
                componentKey = "activityType",
            )
        )

        val dto = objectMapper.readValue<ProcessLinkCreateRequestDto>(json) as? UIComponentProcessLinkCreateRequestDto

        assertThat(dto).isNotNull
        assertThat(dto!!.processLinkType).isEqualTo(TYPE_UI_COMPONENT)
        assertThat(dto!!.componentKey).isEqualTo("activityType")
    }

    @Test
    fun `should serialize UIComponentProcessLinkResponseDto`() {
        val json = objectMapper.writeValueAsString(
            UIComponentProcessLinkResponseDto(
                id = UUID.randomUUID(),
                processDefinitionId = "processDefinitionId",
                activityId = "activityId",
                activityType = ActivityTypeWithEventName.USER_TASK_CREATE,
                componentKey = "componentKey",
            )
        )

        MatcherAssert.assertThat(json, hasJsonPath("$.processLinkType", equalTo(TYPE_UI_COMPONENT)))
        MatcherAssert.assertThat(json, hasJsonPath("$.componentKey", equalTo("componentKey")))
    }


    @Test
    fun `should deserialize UIComponentProcessLinkDeployDto`() {
        val json = objectMapper.writeValueAsString(
            UIComponentProcessLinkDeployDto(
                processDefinitionId = "processDefinitionId",
                activityId = "activityId",
                activityType = ActivityTypeWithEventName.USER_TASK_CREATE,
                componentKey = "componentKey",
            )
        )

        val dto = objectMapper.readValue<ProcessLinkDeployDto>(json) as? UIComponentProcessLinkDeployDto

        assertThat(dto).isNotNull
        assertThat(dto!!.processLinkType).isEqualTo(TYPE_UI_COMPONENT)
        assertThat(dto!!.componentKey).isEqualTo("componentKey")
    }

    @Test
    fun `should deserialize UIComponentProcessLinkExportResponseDto`() {
        val json = objectMapper.writeValueAsString(
            UIComponentProcessLinkExportResponseDto(
                activityId = "activityId",
                activityType = ActivityTypeWithEventName.USER_TASK_CREATE,
                componentKey = "componentKey",
            )
        )

        val dto = objectMapper.readValue<ProcessLinkExportResponseDto>(json) as? UIComponentProcessLinkExportResponseDto

        assertThat(dto).isNotNull
        assertThat(dto!!.processLinkType).isEqualTo(TYPE_UI_COMPONENT)
        assertThat(dto!!.componentKey).isEqualTo("componentKey")
    }


    @Test
    fun `should deserialize UIComponentProcessLinkUpdateRequestDto`() {
        val json = objectMapper.writeValueAsString(
            UIComponentProcessLinkUpdateRequestDto(
                id = UUID.randomUUID(),
                componentKey = "componentKey",
            )
        )

        val dto = objectMapper.readValue<ProcessLinkUpdateRequestDto>(json) as? UIComponentProcessLinkUpdateRequestDto

        assertThat(dto).isNotNull
        assertThat(dto!!.processLinkType).isEqualTo(TYPE_UI_COMPONENT)
        assertThat(dto!!.componentKey).isEqualTo("componentKey")
    }
}