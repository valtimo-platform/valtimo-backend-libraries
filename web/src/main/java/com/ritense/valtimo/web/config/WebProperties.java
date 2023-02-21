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

package com.ritense.valtimo.web.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.web.cors.CorsConfiguration;
import java.util.Set;

@ConfigurationProperties(prefix = "valtimo.web", ignoreUnknownFields = false)
public class WebProperties {

    final CorsPathsConfig cors = new CorsPathsConfig();

    public CorsPathsConfig getCors() {
        return cors;
    }

    public static class CorsPathsConfig {

        private final CorsConfiguration corsConfiguration = new CorsConfiguration();

        private Set<String> paths = WebDefaults.Cors.paths;

        public CorsConfiguration getCorsConfiguration() {
            return corsConfiguration;
        }

        public Set<String> getPaths() {
            return paths;
        }

        public void setPaths(Set<String> paths) {
            this.paths = paths;
        }
    }

}
