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

package com.ritense.formlink.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.ritense.document.domain.Document;
import com.ritense.formlink.domain.FormAssociation;
import com.ritense.formlink.domain.impl.formassociation.CamundaFormAssociation;
import com.ritense.formlink.domain.impl.formassociation.FormAssociationType;
import com.ritense.formlink.domain.request.CreateFormAssociationRequest;
import com.ritense.formlink.domain.request.ModifyFormAssociationRequest;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface FormAssociationService {

    Set<? extends FormAssociation> getAllFormAssociations(String processDefinitionKey);

    Optional<? extends FormAssociation> getFormAssociationById(String processDefinitionKey, UUID id);

    Optional<? extends FormAssociation> getFormAssociationByFormLinkId(String processDefinitionKey, String formLinkId);

    Optional<JsonNode> getFormDefinitionByFormLinkId(String processDefinitionKey, String formLinkId);

    Optional<CamundaFormAssociation> getStartEventFormDefinitionByProcessDefinitionKey(String processDefinitionKey);

    Optional<JsonNode> getStartEventFormDefinition(String processDefinitionKey);

    Optional<JsonNode> getPreFilledFormDefinitionByFormLinkId(
        Document.Id documentId, String processDefinitionKey, String formLinkId
    );

    Optional<JsonNode> getPreFilledFormDefinitionByFormLinkId(
        Document.Id documentId, String processDefinitionKey, String formLinkId, String taskInstanceId
    );

    FormAssociation createFormAssociation(
        String processDefinitionKey,
        String formName,
        String formLinkElementId,
        FormAssociationType type,
        boolean isPublic
    );

    FormAssociation createFormAssociation(CreateFormAssociationRequest request);

    FormAssociation modifyFormAssociation(ModifyFormAssociationRequest request);

    void deleteFormAssociation(String processDefinitionKey, UUID formAssociationId);

}