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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ritense.valtimo.service.GlobalSettingsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE;

@RestController
@RequestMapping(value = "/api", produces = APPLICATION_JSON_UTF8_VALUE)
public class GlobalResource {
    private static final Logger logger = LoggerFactory.getLogger(UserResource.class);
    private final ObjectMapper objectMapper;
    private final GlobalSettingsService globalSettingsService;

    public GlobalResource(
        ObjectMapper objectMapper,
        GlobalSettingsService globalSettingsService
    ) {
        this.objectMapper = objectMapper;
        this.globalSettingsService = globalSettingsService;
    }

    @GetMapping("/v1/settings")
    public ResponseEntity<String> getGlobalSettings() throws JsonProcessingException {
        logger.debug("Request to get global settings");
        var result = globalSettingsService.getGlobalSettings();
        Map<String, Object> settings = Map.of();
        if (result.isPresent()) {
            settings = result.get().getSettings();
        }

        return ResponseEntity.ok(objectMapper.writeValueAsString(settings));
    }

    @PutMapping("/v1/settings")
    public ResponseEntity<Object> saveGlobalSettings(@RequestBody String settings){
        logger.debug("Request to create global settings");
        try{
            Map<String, Object> settingsMap = objectMapper.readValue(settings, new TypeReference<>() {});
            globalSettingsService.saveGlobalSettings(settingsMap);
        } catch (Exception e){
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok().build();
    }
}
