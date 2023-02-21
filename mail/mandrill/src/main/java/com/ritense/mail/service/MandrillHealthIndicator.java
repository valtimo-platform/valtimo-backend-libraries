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

package com.ritense.mail.service;

import com.microtripit.mandrillapp.lutung.MandrillApi;
import com.microtripit.mandrillapp.lutung.model.MandrillApiError;
import com.ritense.mail.config.MandrillProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;

public class MandrillHealthIndicator extends AbstractHealthIndicator {

    public static final String PONG = "PONG!";
    private static final Logger logger = LoggerFactory.getLogger(MandrillHealthIndicator.class);
    private final MandrillApi mandrillApi;

    public MandrillHealthIndicator(MandrillProperties mandrillProperties) {
        this.mandrillApi = mandrillProperties.createMandrillApi();
    }

    @Override
    public void doHealthCheck(Health.Builder builder) throws Exception {
        String response;
        try {
            response = this.mandrillApi.users().ping();
            if (response.equals(PONG)) {
                builder.up();
            } else {
                builder.unknown();
            }
        } catch (MandrillApiError mandrillApiError) {
            builder.down();
        } catch (Exception e) {
            logger.warn("Exception was thrown while checking Mandrill health", e);
            builder.unknown().withException(e);
        }
    }
}