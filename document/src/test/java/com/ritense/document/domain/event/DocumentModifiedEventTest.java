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

package com.ritense.document.domain.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.ritense.document.config.SpringContextHelper;
import com.ritense.document.domain.impl.JsonSchemaDocumentFieldChangedEvent;
import com.ritense.document.domain.impl.JsonSchemaDocumentId;
import com.ritense.document.domain.impl.event.JsonSchemaDocumentModifiedEvent;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.env.MockEnvironment;

class DocumentModifiedEventTest {
    private ApplicationContext applicationContext;
    private MockEnvironment environment;

    @BeforeEach
    public void setUp() {
        applicationContext = mock(ApplicationContext.class);
        environment = new MockEnvironment();
    }

    @Test
    void shouldReturnChangesWhenPropertyIsSet() throws NoSuchFieldException, IllegalAccessException {
        environment.setProperty("valtimo.audit.auditDocumentChanges", "true");

        Field field = SpringContextHelper.class.getDeclaredField("context");
        injectMock(field, applicationContext);
        when(applicationContext.getEnvironment()).thenReturn(environment);

        List<JsonSchemaDocumentFieldChangedEvent> changes = List
            .of(new JsonSchemaDocumentFieldChangedEvent("type", "path", null, null));

        JsonSchemaDocumentModifiedEvent documentModifiedEvent = createDocumentModifiedEvent(changes);

        assertEquals(1, documentModifiedEvent.registeredChanges().size());

    }

    @Test
    void shouldReturnNothingWhenPropertyIsNotSet() throws NoSuchFieldException, IllegalAccessException {
        Field field = SpringContextHelper.class.getDeclaredField("context");
        injectMock(field, applicationContext);
        when(applicationContext.getEnvironment()).thenReturn(environment);

        List<JsonSchemaDocumentFieldChangedEvent> changes = List
            .of(new JsonSchemaDocumentFieldChangedEvent("type", "path", null, null));

        JsonSchemaDocumentModifiedEvent documentModifiedEvent = createDocumentModifiedEvent(changes);

        assertNull(documentModifiedEvent.registeredChanges());

    }

    private void injectMock(Field field, Object newValue) throws IllegalAccessException {
        field.setAccessible(true);
        field.set(null, newValue);

    }

    private JsonSchemaDocumentModifiedEvent createDocumentModifiedEvent(
        List<JsonSchemaDocumentFieldChangedEvent> changes) {
        return new JsonSchemaDocumentModifiedEvent(
            UUID.randomUUID(),
            "some-origin",
            LocalDateTime.now(),
            "some-user",
            JsonSchemaDocumentId.existingId(UUID.randomUUID()),
            changes
        );
    }
}