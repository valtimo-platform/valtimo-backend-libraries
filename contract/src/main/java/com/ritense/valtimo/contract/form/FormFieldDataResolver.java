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

import java.util.Map;
import java.util.UUID;

public interface FormFieldDataResolver {

    @Deprecated(forRemoval = true, since = "9.18")
    default boolean supports(ExternalFormFieldType externalFormFieldType) {
        return false;
    }

    default boolean supports(String externalFormFieldType) {
        return supports(ExternalFormFieldType.fromKey(externalFormFieldType));
    }

    @Deprecated(forRemoval = true, since = "9.21")
    default Map<String, Object> get(String documentDefinitionName, UUID documentId, String... varNames) {
        throw new RuntimeException("The 'get' method should be implemented!");
    }

    default Map<String, Object> get(
        DataResolvingContext dataResolvingContext,
        String... varNames
    ) {
        return get(dataResolvingContext.getDocumentDefinitionName(), dataResolvingContext.getDocumentId(), varNames);
    }

}