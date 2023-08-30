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

package com.ritense.document.domain.impl.listener;

import com.ritense.authorization.AuthorizationContext;
import com.ritense.document.service.DocumentDefinitionService;
import javax.transaction.Transactional;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

public class ApplicationReadyEventListenerImpl {

    private final DocumentDefinitionService documentDefinitionService;

    public ApplicationReadyEventListenerImpl(DocumentDefinitionService documentDefinitionService) {
        this.documentDefinitionService = documentDefinitionService;
    }

    @Transactional
    @EventListener(ApplicationReadyEvent.class)
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public void handle() {
        AuthorizationContext.runWithoutAuthorization(() -> {
            documentDefinitionService.deployAll(true, true);
            return null;
        });
    }
}