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

package com.ritense.document;

import com.ritense.resource.service.ResourceService;
import com.ritense.valtimo.contract.config.ValtimoProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static org.mockito.Mockito.mock;

@SpringBootApplication
@EnableConfigurationProperties(value = {ValtimoProperties.class})
public class DocumentTestConfiguration {

    public static void main(String[] args) {
        SpringApplication.run(DocumentTestConfiguration.class, args);
    }

    @Bean
    public ResourceService resourceService() {
        return mock(ResourceService.class);
    }

    @TestConfiguration
    public static class TestConfig {
        //Beans extra
    }

}