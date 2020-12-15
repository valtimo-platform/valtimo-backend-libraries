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

package com.ritense.form.web.rest;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;

public interface FormResource {

    /**
     * Get form preFilled by formDefinitionName.
     *
     * @param formDefinitionName slugNameOfForm
     * @return JsonNode formDefinition raw json
     * @deprecated This method is no longer acceptable to load a form.
     * <p> Use FormAssociationResource </p> instead.
     */
    @Deprecated(forRemoval = true, since = "5.1.0")
    ResponseEntity<JsonNode> getFormByName(String formDefinitionName);

    /**
     * Get form preFilled by formDefinitionName and documentId.
     *
     * @param formDefinitionName slugNameOfForm
     * @param documentId documentId
     * @return JsonNode formDefinition raw json
     * @deprecated This method is no longer acceptable to load a form.
     * <p> Use FormAssociationResource </p> instead.
     */
    @Deprecated(forRemoval = true, since = "5.1.0")
    ResponseEntity<JsonNode> getFormPreFilled(String formDefinitionName, String documentId);

}