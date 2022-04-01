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

package com.ritense.valtimo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import java.util.Optional;
import java.util.TimeZone;

public class ValtimoApplicationReadyEventListener {

    private static final Logger logger = LoggerFactory.getLogger(ValtimoApplicationReadyEventListener.class);
    private TimeZone defaultTimeZone = TimeZone.getTimeZone("UTC");

    public ValtimoApplicationReadyEventListener(Optional<String> timeZone) {
        timeZone.ifPresent(value -> this.defaultTimeZone = TimeZone.getTimeZone(value));
    }

    @EventListener(ApplicationReadyEvent.class)
    public void handle(ApplicationReadyEvent applicationReadyEvent) {
        TimeZone.setDefault(defaultTimeZone);
        logger.info("Setting Valtimo time zone to {}", defaultTimeZone);
    }

}