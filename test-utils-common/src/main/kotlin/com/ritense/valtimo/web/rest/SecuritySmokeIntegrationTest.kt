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
import org.junit.jupiter.api.Assertions.assertTrue
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
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.mvc.method.RequestMappingInfo
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping

@ExtendWith(SpringExtension::class)
@SpringBootTest
@AutoConfigureMockMvc
@Tag("security")
abstract class SecuritySmokeIntegrationTest(
    private val basePackageName:String? = null,
    private val ignoredPathPatterns: Set<String> = setOf()
) {
    @MockBean
    var userManagementService: UserManagementService? = null

    @MockBean
    var mailSender: MailSender? = null

    @Qualifier("requestMappingHandlerMapping")
    @Autowired
    private val handlerMapping: RequestMappingHandlerMapping? = null

    @Autowired
    private val mockMvc: MockMvc? = null

    @TestFactory
    fun shouldTestAllSimpleEndpointsForAuthentication(): Collection<DynamicTest> {
        val endPointData: Set<Map.Entry<RequestMappingInfo, HandlerMethod>> = handlerMapping!!.handlerMethods.entries

        return endPointData.filter {
            basePackageName == null || it.value.beanType.packageName.startsWith(basePackageName)
        }
            .mapNotNull {
            getDynamicEndpointTest(it)
        }
    }

    private fun getDynamicEndpointTest(
        entry: Map.Entry<RequestMappingInfo, HandlerMethod>
    ): DynamicTest? {
        val requestMapping = entry.key

        val patterns = getPathPatternsCondition(requestMapping)
        if (ignoredPathPatterns.containsAll(patterns)) {
            return null
        }

        val methods = requestMapping.methodsCondition.methods
            .joinToString("|") { it.toString() }

        val testName = "$methods ${patterns.joinToString()}"
        return DynamicTest.dynamicTest(testName) {
            testEndpoint(requestMapping)
        }
    }

    private fun testEndpoint(mappingInfo: RequestMappingInfo) {
        assertDoesNotThrow {
            val path = findSimplePath(mappingInfo)
            val paramMap = getSimpleParamMap(mappingInfo)

            val method = mappingInfo.methodsCondition.methods.firstOrNull()
                ?.let { HttpMethod.valueOf(it.name) }
                ?:HttpMethod.GET

            val request = MockMvcRequestBuilders.request(method, path)
            mappingInfo.consumesCondition.consumableMediaTypes.firstOrNull()
                ?.run { request.contentType(this) }

            request.params(LinkedMultiValueMap(paramMap))
            request.accept("*/*")
            request.with(remoteAddr())
            val mvcResult = mockMvc!!.perform(request)
                .andReturn()
            val statusCode = mvcResult.response.status

            assertTrue(statusCode == 401 || statusCode == 403) { "Expected status 401 or 403, was $statusCode" }
        }
    }

    private fun getSimpleParamMap(mappingInfo: RequestMappingInfo): Map<String, List<String>> {
        val expressions = mappingInfo.paramsCondition.expressions
        return if (expressions.any { it.value != null && it.value!!.matches(".*\\{.*}.*".toRegex()) }) {
            throw RuntimeException("Can't resolve complex param conditions.")
        } else {
            expressions.groupBy({ it.name }, { it.value?: "" })
        }
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