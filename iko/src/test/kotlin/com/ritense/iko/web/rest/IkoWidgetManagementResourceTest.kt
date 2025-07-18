/*
 * Copyright 2015-2025 Ritense BV, the Netherlands.
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

package com.ritense.iko.web.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.jsontype.NamedType
import com.ritense.authorization.permission.condition.ContainerPermissionCondition
import com.ritense.authorization.permission.condition.ExpressionPermissionCondition
import com.ritense.authorization.permission.condition.FieldPermissionCondition
import com.ritense.iko.service.IkoWidgetService
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
import com.ritense.valtimo.contract.json.MapperSingleton
import com.ritense.widget.fields.FieldsWidget
import com.ritense.widget.fields.FieldsWidgetDto
import com.ritense.widget.fields.FieldsWidgetProperties
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional

@Transactional
internal class IkoWidgetManagementResourceTest {

    private lateinit var mockMvc: MockMvc
    private lateinit var resource: IkoWidgetManagementResource
    private lateinit var service: IkoWidgetService
    private lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun init() {
        objectMapper = MapperSingleton.get().copy().apply {
            this.registerSubtypes(FieldsWidget::class.java)
            this.registerSubtypes(FieldsWidgetDto::class.java)
        }

        service = mock()
        resource = IkoWidgetManagementResource(service)
        mockMvc = MockMvcBuilders.standaloneSetup(resource)
            .setCustomArgumentResolvers(PageableHandlerMethodArgumentResolver())
            .setMessageConverters(MappingJackson2HttpMessageConverter(objectMapper))
            .build()
    }

    @Test
    fun `should get iko widgets`() {
        whenever(service.findAllByTabKey("klant", "general")).thenReturn(
            listOf(widget())
        )

        mockMvc.perform(
            get(
                "/api/management/v1/iko-data-aggregate/{dataAggregateKey}/tab/{tabKey}/widget",
                "klant",
                "general"
            )
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].type").value("fields"))
            .andExpect(jsonPath("$[0].key").value("partner"))
            .andExpect(jsonPath("$[0].title").value("Partner"))
            .andExpect(jsonPath("$[0].width").value(3))
            .andExpect(jsonPath("$[0].highContrast").value(false))
            .andExpect(jsonPath("$[0].actions").isEmpty)
            .andExpect(jsonPath("$[0].properties.columns[0][0].key").value("naam"))
            .andExpect(jsonPath("$[0].properties.columns[0][0].title").value("Naam"))
            .andExpect(jsonPath("$[0].properties.columns[0][0].value").value("iko:/persoon/naam/volledigeNaam"))
    }

    @Test
    fun `should get iko widget by key`() {
        whenever(service.getByKey("klant", "general", "general"))
            .thenReturn(widget())

        mockMvc.perform(
            get(
                "/api/management/v1/iko-data-aggregate/{dataAggregateKey}/tab/{tabKey}/widget/{widgetKey}",
                "klant",
                "general",
                "general"
            )
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type").value("fields"))
            .andExpect(jsonPath("$.key").value("partner"))
            .andExpect(jsonPath("$.title").value("Partner"))
            .andExpect(jsonPath("$.width").value(3))
            .andExpect(jsonPath("$.highContrast").value(false))
            .andExpect(jsonPath("$.actions").isEmpty)
            .andExpect(jsonPath("$.properties.columns[0][0].key").value("naam"))
            .andExpect(jsonPath("$.properties.columns[0][0].title").value("Naam"))
            .andExpect(jsonPath("$.properties.columns[0][0].value").value("iko:/persoon/naam/volledigeNaam"))
    }

    @Test
    fun `should create iko widget`() {
        val widget = widget()
        val request = widget.toDto()
        whenever(service.create(eq("klant"), eq("general"), any())).thenReturn(widget)

        mockMvc.perform(
            post(
                "/api/management/v1/iko-data-aggregate/{dataAggregateKey}/tab/{tabKey}/widget/{widgetKey}",
                "klant",
                "general",
                "partner"
            )
                .content(objectMapper.writeValueAsString(request))
                .contentType(APPLICATION_JSON_UTF8_VALUE)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type").value("fields"))
            .andExpect(jsonPath("$.key").value("partner"))
            .andExpect(jsonPath("$.title").value("Partner"))
            .andExpect(jsonPath("$.width").value(3))
            .andExpect(jsonPath("$.highContrast").value(false))
            .andExpect(jsonPath("$.actions").isEmpty)
            .andExpect(jsonPath("$.properties.columns[0][0].key").value("naam"))
            .andExpect(jsonPath("$.properties.columns[0][0].title").value("Naam"))
            .andExpect(jsonPath("$.properties.columns[0][0].value").value("iko:/persoon/naam/volledigeNaam"))
    }

    @Test
    fun `should update iko widget`() {
        val widget = widget()
        val request = widget.toDto()
        whenever(service.findAllByTabKey("klant", "general")).thenReturn(
            listOf(widget())
        )
        whenever(service.update(eq("klant"), eq("general"), any()))
            .thenReturn(widget)
        mockMvc.perform(
            put(
                "/api/management/v1/iko-data-aggregate/{dataAggregateKey}/tab/{tabKey}/widget",
                "klant",
                "general"
            )
                .content("""[${objectMapper.writeValueAsString(request)}]""")
                .contentType(APPLICATION_JSON_UTF8_VALUE)
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].type").value("fields"))
            .andExpect(jsonPath("$[0].key").value("partner"))
            .andExpect(jsonPath("$[0].title").value("Partner"))
            .andExpect(jsonPath("$[0].width").value(3))
            .andExpect(jsonPath("$[0].highContrast").value(false))
            .andExpect(jsonPath("$[0].actions").isEmpty)
            .andExpect(jsonPath("$[0].properties.columns[0][0].key").value("naam"))
            .andExpect(jsonPath("$[0].properties.columns[0][0].title").value("Naam"))
            .andExpect(jsonPath("$[0].properties.columns[0][0].value").value("iko:/persoon/naam/volledigeNaam"))
    }

    @Test
    fun `should delete iko widget`() {
        mockMvc.perform(
            delete(
                "/api/management/v1/iko-data-aggregate/{dataAggregateKey}/tab/{tabKey}/widget/{widgetKey}",
                "klant",
                "general",
                "general"
            )
        )
            .andDo(print())
            .andExpect(status().isNoContent())
    }

    private fun widget() = FieldsWidget(
        key = "partner",
        title = "Partner",
        order = 0,
        width = 3,
        highContrast = false,
        properties = FieldsWidgetProperties(
            listOf(
                listOf(
                    FieldsWidgetProperties.Field(
                        key = "naam",
                        title = "Naam",
                        value = "iko:/persoon/naam/volledigeNaam"
                    )
                )
            )
        )
    )
}
