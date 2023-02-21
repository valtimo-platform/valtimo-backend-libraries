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

package com.ritense.valtimo.web.rest;

import org.springframework.web.bind.annotation.RequestMethod;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class EndpointSecurityResult {
    private final Set<RequestMethod> methods;
    private final Set<String> patterns;
    private final Set<String> paramConditions;
    private final SecuritySmokeIntegrationTest.SecurityResult securityResult;
    private final SecuritySmokeIntegrationTest.TestResult testResult;
    private final Collection<String> classSecurityAnnotations;
    private final Collection<String> methodSecurityAnnotations;
    private final boolean openForLocalNetwork;
    private final boolean openForPublicNetwork;
    private final String javaMethod;

    private EndpointSecurityResult(
        Set<RequestMethod> methods,
        Set<String> patterns,
        Set<String> paramConditions,
        SecuritySmokeIntegrationTest.SecurityResult securityResult,
        SecuritySmokeIntegrationTest.TestResult testResult,
        Collection<String> classSecurityAnnotations,
        Collection<String> methodSecurityAnnotations,
        boolean openForLocalNetwork,
        boolean openForPublicNetwork,
        String javaMethod
    ) {
        this.methods = methods;
        this.patterns = patterns;
        this.paramConditions = paramConditions;
        this.securityResult = securityResult;
        this.testResult = testResult;
        this.classSecurityAnnotations = classSecurityAnnotations;
        this.methodSecurityAnnotations = methodSecurityAnnotations;
        this.openForLocalNetwork = openForLocalNetwork;
        this.openForPublicNetwork = openForPublicNetwork;
        this.javaMethod = javaMethod;
    }

    public SecuritySmokeIntegrationTest.TestResult getTestResult() {
        return testResult;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Set<RequestMethod> methods;
        private Set<String> patterns;
        private Set<String> paramConditions;
        private SecuritySmokeIntegrationTest.SecurityResult securityResult;
        private SecuritySmokeIntegrationTest.TestResult testResult;
        private Collection<String> classSecurityAnnotations;
        private Collection<String> methodSecurityAnnotations;
        private boolean openForLocalNetwork;
        private boolean openForPublicNetwork;
        private String javaMethod;

        public Builder methods(Set<RequestMethod> methods) {
            this.methods = methods;
            return this;
        }

        public Builder patterns(Set<String> patterns) {
            this.patterns = patterns;
            return this;
        }

        public Builder paramConditions(Set<String> paramConditions) {
            this.paramConditions = paramConditions;
            return this;
        }

        public Builder securityResult(SecuritySmokeIntegrationTest.SecurityResult securityResult) {
            this.securityResult = securityResult;
            return this;
        }

        public Builder testResult(SecuritySmokeIntegrationTest.TestResult testResult) {
            this.testResult = testResult;
            return this;
        }

        public Builder classSecurityAnnotations(Collection<String> classSecurityAnnotations) {
            this.classSecurityAnnotations = classSecurityAnnotations;
            return this;
        }

        public Builder methodSecurityAnnotations(Collection<String> methodSecurityAnnotations) {
            this.methodSecurityAnnotations = methodSecurityAnnotations;
            return this;
        }

        public Builder openForLocalNetwork(boolean openForLocalNetwork) {
            this.openForLocalNetwork = openForLocalNetwork;
            return this;
        }

        public Builder openForPublicNetwork(boolean openForPublicNetwork) {
            this.openForPublicNetwork = openForPublicNetwork;
            return this;
        }

        public Builder javaMethod(String javaMethod) {
            this.javaMethod = javaMethod;
            return this;
        }

        public EndpointSecurityResult build() {
            return new EndpointSecurityResult(methods,
                patterns,
                paramConditions,
                securityResult,
                testResult,
                classSecurityAnnotations,
                methodSecurityAnnotations,
                openForLocalNetwork,
                openForPublicNetwork,
                javaMethod
            );
        }
    }

    public String toPrettyString() {
        return String.format("Endpoint Security Result:\r\n" +
                "methods: %s\r\n" +
                "patterns: %s\r\n" +
                "paramConditions: %s\r\n" +
                "securityResult: %s\r\n" +
                "testResult: %s\r\n" +
                "classSecurityAnnotations: %s\r\n" +
                "methodSecurityAnnotations: %s\r\n" +
                "openForLocalNetwork: %s\r\n" +
                "openForPublicNetwork: %s\r\n" +
                "javaMethod: %s",
            methods.stream().map(Object::toString).collect(Collectors.joining(", ")),
            String.join(", ", patterns),
            String.join(", ", paramConditions),
            securityResult,
            testResult,
            String.join(", ", classSecurityAnnotations),
            String.join(", ", methodSecurityAnnotations),
            openForLocalNetwork,
            openForPublicNetwork,
            javaMethod
        );
    }

    @Override
    public String toString() {
        return "EndpointSecurityResult{" +
            "methods=" + methods +
            ", patterns=" + patterns +
            ", paramConditions=" + paramConditions +
            ", securityResult=" + securityResult +
            ", testResult=" + testResult +
            ", classSecurityAnnotations=" + classSecurityAnnotations +
            ", methodSecurityAnnotations=" + methodSecurityAnnotations +
            ", openForLocalNetwork=" + openForLocalNetwork +
            ", openForPublicNetwork=" + openForPublicNetwork +
            ", javaMethod='" + javaMethod + '\'' +
            '}';
    }
}
