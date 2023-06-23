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
package com.ritense.valtimo.web.rest

import com.ritense.valtimo.contract.authentication.UserManagementService
import com.ritense.valtimo.contract.mail.MailSender
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpMethod
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.RequestPostProcessor
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.mvc.method.RequestMappingInfo
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping

/**
 * This abstract test class generates security test for each endpoint in the Spring request handler mapping.
 * Each generated test will check if 'some' security has been applied to the endpoint by attempting to perform a request to it.
 * IMPORTANT: The result of this test should be considered indicative. It does not check if the proper authentication or authorization rules have been applied.
 *
 * When performing the requests, required parameters will be filled with a dummy value where possible.
 *
 * @param basePackageName Tests will only be generated for endpoints of which the handlers are defined within the given package. When null, no filter is applied.
 * @param ignoredPathPatterns These request patterns will be ignored. This could be used when an endpoint is insecure on purpose or when the specific requests is too complex to test properly. Patterns typically look like this: 'GET /xyz'
 */
@ExtendWith(SpringExtension::class)
@SpringBootTest
@AutoConfigureMockMvc
@Tag("security")
abstract class SecuritySmokeIntegrationTest(
    private val basePackageName: String? = null,
    private val ignoredPathPatterns: Set<String> = setOf()
) {
    @MockBean
    private lateinit var userManagementService: UserManagementService

    @MockBean
    private lateinit var mailSender: MailSender

    @Qualifier("requestMappingHandlerMapping")
    @Autowired
    private lateinit var handlerMapping: RequestMappingHandlerMapping

    @Autowired
    private lateinit var mockMvc: MockMvc

    @TestFactory
    fun shouldTestAllSimpleEndpointsForAuthentication(): Collection<DynamicTest> {
        val endPointData: Set<Map.Entry<RequestMappingInfo, HandlerMethod>> = handlerMapping.handlerMethods.entries

        return endPointData.filter {
            basePackageName == null || it.value.beanType.packageName.startsWith(basePackageName)
        }.mapNotNull {
            createEndpointTest(it.key, it.value)
        }
    }

    private fun createEndpointTest(
        mappingInfo: RequestMappingInfo, handlerMethod: HandlerMethod
    ): DynamicTest? {

        val testNames = mappingInfo.methodsCondition.methods
            .ifEmpty { setOf(null) }
            .map { it?.toString() ?: "" }
            .sorted()
            .flatMap { method ->
                getPathPatternsCondition(mappingInfo)
                    .sorted()
                    .map { path ->
                        "$method $path"
                    }
            }

        return if (testNames.isEmpty()) {
            null
        } else if (testNames.any { testName -> ignoredPathPatterns.contains(testName) }) {
            DynamicTest.dynamicTest("Ignored: ${testNames.first()}") {}
        } else {
            DynamicTest.dynamicTest(testNames.first()) {
                testEndpoint(mappingInfo, handlerMethod)
            }
        }
    }

    private fun testEndpoint(mappingInfo: RequestMappingInfo, handlerMethod: HandlerMethod) {
        assertDoesNotThrow {
            val path = findSimplePath(mappingInfo)
            val paramMap = getSimpleParamMap(mappingInfo, handlerMethod)

            val method = mappingInfo.methodsCondition.methods.firstOrNull()
                ?.let { HttpMethod.valueOf(it.name) }
                ?: HttpMethod.GET

            val request = MockMvcRequestBuilders.request(method, path)
            mappingInfo.consumesCondition.consumableMediaTypes.firstOrNull()
                ?.run { request.contentType(this) }

            request.params(LinkedMultiValueMap(paramMap))
            request.accept("*/*")
            request.with(remoteAddr())
            val mvcResult = mockMvc.perform(request)
                .andDo(print())
                .andReturn()
            val statusCode = mvcResult.response.status

            assert(statusCode == 401 || statusCode == 403) { "Expected status 401 or 403, was $statusCode." }
        }
    }

    private fun getSimpleParamMap(
        mappingInfo: RequestMappingInfo,
        handlerMethod: HandlerMethod
    ): Map<String, List<String>> {
        val expressions = mappingInfo.paramsCondition.expressions
            .map {
                val value = if (it.value != null && it.value!!.matches(".*\\{.*}.*".toRegex())) {
                    null //We can't guess complex expression values
                } else {
                    it.value
                }
                Pair(it.name, value ?: "")
            }

        val methodParams = handlerMethod.methodParameters.mapNotNull {
            val requestParam = it.getParameterAnnotation(RequestParam::class.java)
            return@mapNotNull requestParam?.let {
                if (!requestParam.required) {
                    null
                } else {
                    val name = if (requestParam.name != "") requestParam.name else requestParam.value
                    Pair(name, "")
                }
            }
        }

        return (expressions + methodParams)
            .groupBy({ it.first }, { it.second })
            .map { entry -> Pair(entry.key, listOf(entry.value.maxOf { it })) }
            .toMap()
    }

    private fun findSimplePath(key: RequestMappingInfo): String {
        val patterns = getPathPatternsCondition(key)
        return patterns.firstOrNull { s -> !s.matches(".*\\{.*:.*}.*".toRegex()) }
            ?.replace("\\{.*}".toRegex(), "1337")
            ?: throw RuntimeException("No simple path found.")
    }

    private fun getPathPatternsCondition(key: RequestMappingInfo): Set<String> {
        return key.pathPatternsCondition
            ?.patterns
            ?.map { it.patternString }
            ?.toSet()
            ?: setOf()
    }

    companion object {
        private fun remoteAddr(remoteAddr: String = "8.8.8.8"): RequestPostProcessor {
            return RequestPostProcessor { request: MockHttpServletRequest ->
                request.remoteAddr = remoteAddr
                request
            }
        }
    }
}