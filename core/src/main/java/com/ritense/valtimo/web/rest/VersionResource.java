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

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class VersionResource {

    @GetMapping(value = "/v1/valtimo/version")
    public ResponseEntity<Map<String, String>> getValtimoVersion() {
        String title = "";
        if (this.getClass().getPackage().getImplementationTitle() != null) {
            title = this.getClass().getPackage().getImplementationTitle();
        }

        String versionNumber = "";
        if (this.getClass().getPackage().getImplementationVersion() != null) {
            versionNumber = this.getClass().getPackage().getImplementationVersion();
        }

        return ResponseEntity.ok(
            Map.of("title", title, "version", versionNumber)
        );
    }

}