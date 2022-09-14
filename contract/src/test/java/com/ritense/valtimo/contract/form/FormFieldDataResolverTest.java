/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

package com.ritense.valtimo.contract.form;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FormFieldDataResolverTest {

    @Test
    public void deprecatedSupportsReturnsFalse() {
        FormFieldDataResolver resolver = new FormFieldDataResolverImpl();
        assertFalse(resolver.supports(ExternalFormFieldType.OZ));
    }

    @Test
    public void supportsCallsDeprecatedSupportsWithExternalFormFieldType() {
        FormFieldDataResolver resolver = new FormFieldDataResolverImpl();
        assertFalse(resolver.supports("OZ"));
    }

    @Test
    public void supportsFailsWithNotExistingExternalFormFieldType() {
        FormFieldDataResolver resolver = new FormFieldDataResolverImpl();
        IllegalStateException ise = assertThrows(IllegalStateException.class, () ->
            resolver.supports("something-random"));
        assertEquals("Cannot create ExternalFormFieldType from string something-random", ise.getMessage());
    }

    private static class FormFieldDataResolverImpl implements FormFieldDataResolver {
        @Override
        public Map<String, Object> get(String documentDefinitionName, UUID documentId, String... varNames) {
            return null;
        }

        @Override
        public Map<String, Object> get(String documentDefinitionName, UUID documentId, JsonNode formDefinition, String... varNames) {
            return null;
        }
    }
}