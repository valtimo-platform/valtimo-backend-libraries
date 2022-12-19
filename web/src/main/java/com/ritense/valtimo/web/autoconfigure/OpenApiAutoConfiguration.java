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

package com.ritense.valtimo.web.autoconfigure;

import com.ritense.valtimo.web.config.OpenApiProperties;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI configuration.
 */
@Configuration
@EnableConfigurationProperties(value = {OpenApiProperties.class})
public class OpenApiAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(OpenApiAutoConfiguration.class);

    /**
     * OpenAPI configuration.
     *
     * @param openApiProperties the OpenAPI configuration properties
     * @return the OpenAPI configuration
     */
    @Bean
    @ConditionalOnMissingBean(name = "valtimoOpenAPI")
    public OpenAPI valtimoOpenAPI(OpenApiProperties openApiProperties) {
        Contact contact = new Contact()
            .name(openApiProperties.getContactName())
            .url(openApiProperties.getContactUrl())
            .email(openApiProperties.getContactEmail());

        License license = new License()
            .name(openApiProperties.getLicense())
            .url(openApiProperties.getLicenseUrl());

        OpenAPI openAPI = new OpenAPI()
            .info(
                new Info()
                    .title(openApiProperties.getTitle())
                    .description(openApiProperties.getDescription())
                    .version(openApiProperties.getVersion())
                    .contact(contact)
                    .license(license)
                    .termsOfService(openApiProperties.getTermsOfServiceUrl())
            );

        logger.debug("Created OpenAPI configuration");
        return openAPI;
    }

}
