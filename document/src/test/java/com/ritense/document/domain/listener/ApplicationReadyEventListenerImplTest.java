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

package com.ritense.document.domain.listener;

import com.ritense.document.domain.impl.listener.ApplicationReadyEventListenerImpl;
import com.ritense.document.service.DocumentDefinitionService;
import com.ritense.document.service.impl.JsonSchemaDocumentDefinitionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ApplicationReadyEventListenerImplTest {

    private ApplicationReadyEventListenerImpl applicationReadyEventListener;
    private DocumentDefinitionService documentDefinitionService;

    @BeforeEach
    public void setUp() {
        documentDefinitionService = mock(JsonSchemaDocumentDefinitionService.class);
        applicationReadyEventListener = new ApplicationReadyEventListenerImpl(documentDefinitionService);
    }

    @Test
    public void handle() {
        applicationReadyEventListener.handle();
        verify(documentDefinitionService).deployAll(true, true);
    }

}