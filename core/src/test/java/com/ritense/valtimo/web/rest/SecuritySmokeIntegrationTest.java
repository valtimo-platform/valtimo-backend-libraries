/*
 * Copyright 2015-2020 Ritense BV, the Netherlands.
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

package com.ritense.valtimo.web.rest;

import com.ritense.valtimo.contract.authentication.UserManagementService;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.SecurityConfig;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.NameValueExpression;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@Tag("security")
public abstract class SecuritySmokeIntegrationTest {

    @MockBean
    public UserManagementService userManagementService;

    @Autowired
    private RequestMappingHandlerMapping handlerMapping;

    @Autowired
    private MockMvc mockMvc;

    @TestFactory
    public Collection<DynamicTest> shouldTestAllSimpleEndpointsForAuthentication() {
        Set<Map.Entry<RequestMappingInfo, HandlerMethod>> endPointData = handlerMapping.getHandlerMethods().entrySet();
        Set<String> ignoredPathPatterns = getIgnoredPathPatterns();
        List<DynamicTest> dynamicTests = new ArrayList<>(endPointData.size());
        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : endPointData) {
            dynamicTests.add(getDynamicEndpointTest(entry, ignoredPathPatterns));
        }
        return dynamicTests;
    }

    protected abstract Set<String> getIgnoredPathPatterns();

    private DynamicTest getDynamicEndpointTest(
        Map.Entry<RequestMappingInfo, HandlerMethod> entry,
        Set<String> ignoredPathPatterns
    ) {
        final RequestMappingInfo key = entry.getKey();
        final HandlerMethod handlerMethod = entry.getValue();

        final String methods = key.getMethodsCondition().getMethods().stream().map(Object::toString).collect(Collectors.joining("|"));
        final Set<String> patterns = key.getPatternsCondition().getPatterns();

        String testName = String.format("%s %s", methods, String.join(", ", patterns));

        return DynamicTest.dynamicTest(testName, () -> {
            EndpointSecurityResult.Builder builder = EndpointSecurityResult.builder()
                .methods(key.getMethodsCondition().getMethods())
                .paramConditions(key.getParamsCondition().getExpressions().stream().map(Object::toString).collect(Collectors.toSet()))
                .patterns(patterns)
                .classSecurityAnnotations(getSecurityAnnotations(handlerMethod.getMethod().getDeclaringClass().getAnnotations()))
                .methodSecurityAnnotations(getSecurityAnnotations(handlerMethod.getMethod().getDeclaredAnnotations()))
                .javaMethod(handlerMethod.getMethod().getDeclaringClass().getName() + "." + handlerMethod.getMethod().getName());

            SecurityResult securityResult = testEndpoint(key, true);
            if (securityResult == SecurityResult.INSECURE) {
                builder.openForLocalNetwork(true);
                securityResult = testEndpoint(key, false);
                builder.openForPublicNetwork(securityResult == SecurityResult.INSECURE);
            }

            builder.securityResult(securityResult);

            if (ignoredPathPatterns.containsAll(patterns)) {
                builder.testResult(TestResult.IGNORED);
            } else if (securityResult == SecurityResult.SECURE) {
                builder.testResult(TestResult.PASS);
            } else {
                builder.testResult(TestResult.FAIL);
            }

            EndpointSecurityResult result = builder.build();
            assertThat(result.getTestResult())
                .as(result.toPrettyString())
                .isNotEqualByComparingTo(TestResult.FAIL);
        });
    }

    private Set<String> getSecurityAnnotations(Annotation[] annotations) {
        String packageName = SecurityConfig.class.getPackage().getName();
        return Arrays.stream(annotations)
            .filter(annotation -> annotation.annotationType().getPackage().getName().startsWith(packageName))
            .map(annotation -> annotation.annotationType().getSimpleName() + "(\"" + getAnnotationValue(annotation) + "\")")
            .collect(Collectors.toSet());
    }

    private String getAnnotationValue(Annotation annotation) {
        try {
            return Arrays.stream(annotation.annotationType().getMethods())
                .filter(method -> method.getName().equals("value"))
                .filter(method -> !method.getReturnType().equals(Void.TYPE))
                .findFirst()
                .flatMap(method -> {
                    try {
                        Object valueResult = method.invoke(annotation);
                        if (!method.getReturnType().isArray()) {
                            valueResult = new Object[] {valueResult};
                        }
                        return Optional.of((Object[]) valueResult);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .map(valueObject -> Arrays.stream(valueObject)
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .collect(Collectors.joining("\", \"")))
                .orElse("");
        } catch (Exception ex) {
            return "";
        }
    }

    private SecurityResult testEndpoint(RequestMappingInfo mappingInfo, boolean useLocalNetwork) {
        Optional<String> path = findSimplePath(mappingInfo);
        Optional<MultiValueMap<String, String>> paramMap = getSimpleParamMap(mappingInfo);

        if (path.isEmpty()
            || paramMap.isEmpty()) {
            return SecurityResult.UNKNOWN;
        }

        HttpMethod method = mappingInfo.getMethodsCondition().getMethods().stream().findFirst()
            .map(requestMethod -> HttpMethod.valueOf(requestMethod.name()))
            .orElse(HttpMethod.GET);

        try {
            MockHttpServletRequestBuilder request = MockMvcRequestBuilders.request(method, path.get());
            mappingInfo.getConsumesCondition().getConsumableMediaTypes().stream().findFirst()
                .ifPresent(request::contentType);

            request.params(paramMap.get());
            request.accept("*/*");

            request.with(remoteAddr(useLocalNetwork ? "127.0.0.1" : "8.8.8.8"));

            MvcResult mvcResult = mockMvc.perform(request)
                .andReturn();

            int statusCode = mvcResult.getResponse().getStatus();
            if (Arrays.asList(HttpStatus.UNAUTHORIZED.value(), HttpStatus.FORBIDDEN.value()).contains(statusCode)) {
                return SecurityResult.SECURE;
            } else {
                return SecurityResult.INSECURE;
            }
        } catch (Exception ex) {
            return SecurityResult.UNKNOWN;
        }
    }

    private Optional<MultiValueMap<String, String>> getSimpleParamMap(RequestMappingInfo mappingInfo) {
        Set<NameValueExpression<String>> expressions = mappingInfo.getParamsCondition().getExpressions();

        if (expressions.stream()
            .filter(expr -> expr.getValue() != null)
            .anyMatch(expr -> expr.getValue().matches(".*\\{.*\\}.*"))
        ) {
            //I don't know how to resolve these parameters
            return Optional.empty();
        }

        Map<String, List<String>> params = expressions.stream().collect(Collectors.groupingBy(
            NameValueExpression::getName,
            Collectors.mapping(NameValueExpression::getValue, Collectors.toList())));

        return Optional.of(new LinkedMultiValueMap<>(params));
    }

    private static RequestPostProcessor remoteAddr(final String remoteAddr) {
        return request -> {
            request.setRemoteAddr(remoteAddr);
            return request;
        };
    }

    private Optional<String> findSimplePath(RequestMappingInfo key) {
        Set<String> patterns = key.getPatternsCondition().getPatterns();
        return patterns.stream()
            .filter(s -> !s.matches(".*\\{.*:.*\\}.*"))
            .findFirst()
            .map(s -> s.replaceAll("\\{.*\\}", "1337"));
    }

    enum SecurityResult {
        SECURE,
        INSECURE,
        UNKNOWN
    }

    enum TestResult {
        PASS,
        FAIL,
        IGNORED
    }

}