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

import com.ritense.valtimo.web.config.SwaggerProperties;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StopWatch;

/**
 * Springfox Swagger configuration.
 * Warning! When having a lot of REST endpoints, Springfox can become a performance issue. In that
 * case, you can use a specific Spring profile for this class, so that only front-end developers
 * have access to the Swagger view.
 */
@Configuration
@EnableConfigurationProperties(value = {SwaggerProperties.class})
@ConditionalOnProperty(value = {"valtimo.swagger.enabled"})
public class SwaggerAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(SwaggerAutoConfiguration.class);

    /**
     * Swagger Springfox configuration.
     *
     * @param swaggerProperties the swagger configuration properties
     * @return the Swagger Springfox configuration
     */
    @Bean
    @ConditionalOnMissingBean(name = "valtimoOpenAPI")
    public OpenAPI valtimoOpenAPI(SwaggerProperties swaggerProperties) {
        logger.debug("Starting Swagger");
        StopWatch watch = new StopWatch();
        watch.start();

        Contact contact = new Contact()
            .name(swaggerProperties.getContactName())
            .url(swaggerProperties.getContactUrl())
            .email(swaggerProperties.getContactEmail());

        License license = new License()
            .name(swaggerProperties.getLicense())
            .url(swaggerProperties.getLicenseUrl());

        OpenAPI openAPI = new OpenAPI()
            .info(
                new Info()
                    .title(swaggerProperties.getTitle())
                    .description(swaggerProperties.getDescription())
                    .version(swaggerProperties.getVersion())
                    .contact(contact)
                    .license(license)
                    .termsOfService(swaggerProperties.getTermsOfServiceUrl())
            );

        watch.stop();
        logger.debug("Started Swagger in {} ms", watch.getTotalTimeMillis());
        return openAPI;
    }

}
