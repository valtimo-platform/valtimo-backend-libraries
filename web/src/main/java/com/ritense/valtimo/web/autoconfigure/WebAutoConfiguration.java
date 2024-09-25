/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

package com.ritense.valtimo.web.autoconfigure;

import com.ritense.valtimo.contract.hardening.config.HardeningProperties;
import com.ritense.valtimo.contract.hardening.service.HardeningService;
import com.ritense.valtimo.contract.hardening.service.impl.HardeningServiceImpl;
import com.ritense.valtimo.web.config.WebProperties;
import com.ritense.valtimo.web.rest.error.WebModuleExceptionTranslator;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.zalando.problem.jackson.ProblemModule;
import org.zalando.problem.violations.ConstraintViolationProblemModule;

@AutoConfiguration
@EnableConfigurationProperties(WebProperties.class)
public class WebAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(WebAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    public CorsFilter corsFilter(WebProperties webProperties) {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration config = webProperties.getCors().getCorsConfiguration();
        final List<String> allowedOrigins = config.getAllowedOrigins();
        if (allowedOrigins != null && !allowedOrigins.isEmpty()) {
            logger.debug("Registering CORS filter");
            if (!webProperties.getCors().getPaths().isEmpty()) {
                webProperties.getCors().getPaths().forEach(path -> source.registerCorsConfiguration(path, config));
            }
        }
        return new CorsFilter(source);
    }

    @Bean
    @ConditionalOnMissingBean
    public HardeningService hardeningService(HardeningProperties hardeningProperties) {
        return new HardeningServiceImpl(hardeningProperties);
    }

    @Order(0)
    @Bean
    @ConditionalOnMissingBean
    public WebModuleExceptionTranslator webModuleExceptionTranslator(Optional<HardeningService> hardeningService) {
        return new WebModuleExceptionTranslator(hardeningService);
    }

    /*
     * Module for serialization/deserialization of RFC7807 Problem.
     */
    @Bean
    public ProblemModule problemModule() {
        return new ProblemModule();
    }

    /*
     * Module for serialization/deserialization of ConstraintViolationProblem.
     */
    @Bean
    public ConstraintViolationProblemModule constraintViolationProblemModule() {
        return new ConstraintViolationProblemModule();
    }

}