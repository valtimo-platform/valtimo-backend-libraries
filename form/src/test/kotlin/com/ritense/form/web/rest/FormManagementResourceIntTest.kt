package com.ritense.form.web.rest

import com.jayway.jsonpath.JsonPath
import com.ritense.form.BaseIntegrationTest
import com.ritense.form.domain.request.CreateFormDefinitionRequest
import com.ritense.form.domain.request.ModifyFormDefinitionRequest
import com.ritense.valtimo.contract.authentication.ManageableUser
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
import com.ritense.valtimo.contract.utils.TestUtil
import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder
import java.nio.charset.StandardCharsets
import java.util.UUID

@Transactional
class FormManagementResourceIntTest : BaseIntegrationTest() {

    @Autowired
    lateinit var formManagementResource: FormManagementResource

    lateinit var mockMvc: MockMvc

    @BeforeEach
    internal fun init() {
        mockMvc = MockMvcBuilders.standaloneSetup(formManagementResource)
            .setCustomArgumentResolvers(PageableHandlerMethodArgumentResolver())
            .alwaysDo<StandaloneMockMvcBuilder>(MockMvcResultHandlers.print())
            .build()

        val manageableUser: ManageableUser = mock()
        whenever(manageableUser.username).thenReturn("userIdentifier")
        whenever(manageableUser.fullName).thenReturn("FullName")
        whenever(userManagementService.currentUser).thenReturn(manageableUser)
        whenever(userManagementService.findByUsername(any())).thenReturn(manageableUser)
    }

    @Test
    @WithMockUser
    fun `should find forms for case definition`() {
        mockMvc.perform(
            get(
                BASE_URL,
                "person",
                "1.0.0"
            ).accept(APPLICATION_JSON_UTF8_VALUE)
                .contentType(APPLICATION_JSON_UTF8_VALUE)
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isNotEmpty)
            .andExpect(jsonPath("$.totalElements").isNumber)
    }

    @Test
    @WithMockUser
    fun `should not find forms for unknown case definition`() {
        mockMvc.perform(
            get(
                BASE_URL,
                "person",
                "5.0.0"
            ).accept(APPLICATION_JSON_UTF8_VALUE)
                .contentType(APPLICATION_JSON_UTF8_VALUE)
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isEmpty)
            .andExpect(jsonPath("$.totalElements").value(0))
    }

    @Test
    @WithMockUser
    fun `should find forms for case definition with search term`() {
        mockMvc.perform(
            get(
                "$BASE_URL?searchTerm=editgrid",
                "person",
                "1.0.0"
            ).accept(APPLICATION_JSON_UTF8_VALUE)
                .contentType(APPLICATION_JSON_UTF8_VALUE)
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isNotEmpty)
            .andExpect(jsonPath("$.totalElements").value(1))
    }

    @Test
    @WithMockUser
    fun `should not find forms for case definition with search term`() {
        mockMvc.perform(
            get(
                "$BASE_URL?searchTerm=%s",
                "person",
                "5.0.0",
                "nothing"
            ).accept(APPLICATION_JSON_UTF8_VALUE)
                .contentType(APPLICATION_JSON_UTF8_VALUE)
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isEmpty)
            .andExpect(jsonPath("$.totalElements").value(0))
    }

    @Test
    @WithMockUser
    fun `should create form for case definition`() {
        val request = CreateFormDefinitionRequest(DEFAULT_FORM_DEFINITION_NAME, "{}", false)
        mockMvc.perform(
            MockMvcRequestBuilders.post(BASE_URL,
                "person",
                "1.0.0"
            ).characterEncoding(StandardCharsets.UTF_8.name())
                .content(TestUtil.convertObjectToJsonBytes(request))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())

        val savedDefinition = formDefinitionRepository.findByNameAndCaseDefinitionId(
            DEFAULT_FORM_DEFINITION_NAME,
            CaseDefinitionId.of("person", "1.0.0")
        )
        Assertions.assertThat(savedDefinition).isPresent()
    }

    @Test
    @WithMockUser
    fun `should update form for case definition`() {
        val request = CreateFormDefinitionRequest(DEFAULT_FORM_DEFINITION_NAME, "{}", false)
        val result = mockMvc.perform(
            MockMvcRequestBuilders.post(BASE_URL,
                "person",
                "1.0.0"
            )
                .characterEncoding(StandardCharsets.UTF_8.name())
                .content(TestUtil.convertObjectToJsonBytes(request))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())
            .andReturn()

        val documentContext = JsonPath.parse(result.response.contentAsString)
        val id = UUID.fromString(documentContext.read<Any>("$['id']").toString())
        val newDefinition = "{\"key\":\"someValue\"}"

        mockMvc.perform(
            MockMvcRequestBuilders.put(BASE_URL,
                "person",
                "1.0.0"
            )
                .content(
                    TestUtil.convertObjectToJsonBytes(
                        ModifyFormDefinitionRequest(id, DEFAULT_FORM_DEFINITION_NAME, newDefinition)
                    )
                )
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())

        val savedDefinition = formDefinitionRepository.findById(id)
        Assertions.assertThat(savedDefinition).isPresent()
        Assertions.assertThat(savedDefinition.get().formDefinition).hasToString(newDefinition)
    }

    @Test
    @WithMockUser
    @Transactional(Transactional.TxType.NEVER)
    fun `should delete form for case definition`() {
        val savedFormDefinition = formDefinitionRepository.save(formDefinition())

        Assertions.assertThat(formDefinitionRepository.existsById(savedFormDefinition.id)).isTrue()

        mockMvc.perform(
            MockMvcRequestBuilders.delete("$BASE_URL/{formDefinitionId}",
                "person",
                "1.0.0",
                savedFormDefinition.id
            ).contentType(MediaType.APPLICATION_JSON_VALUE)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isNoContent())

        formDefinitionRepository.flush()

        Assertions.assertThat(formDefinitionRepository.existsById(savedFormDefinition.id)).isFalse()
    }

    companion object {
        const val BASE_URL = "/api/management/v1/case-definition/{caseDefinitionKey}/version/{versionTag}/form"
    }
}