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

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * A simple endpoint that always returns HTTP Status Code 200 regardless of authentication.
 * This can be used for health checks (whether the application is running or not).
 * In contrast to Spring Actuator's /management/health, this endpoint doesn't fail on underlying problems,
 * nor does it require authentication to access (which is important for some types of health checks).
 */
@RestController
@RequestMapping(value = "/api/v1/ping")
public class PingResource {

    private static final String PING_RESPONSE = "pong";

    @GetMapping(produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public String pingPong() {
        return PING_RESPONSE;
    }

}