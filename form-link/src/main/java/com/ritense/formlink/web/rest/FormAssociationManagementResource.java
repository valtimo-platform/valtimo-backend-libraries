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

package com.ritense.formlink.web.rest;

import com.ritense.formlink.domain.FormAssociation;
import com.ritense.formlink.domain.request.CreateFormAssociationRequest;
import com.ritense.formlink.domain.request.ModifyFormAssociationRequest;
import org.springframework.http.ResponseEntity;
import java.util.Collection;

public interface FormAssociationManagementResource {

    ResponseEntity<Collection<? extends FormAssociation>> getAll(String processDefinitionKey);

    ResponseEntity<? extends FormAssociation> getFormAssociationById(String processDefinitionKey, String id);

    ResponseEntity<? extends FormAssociation> getFormAssociationByFormLinkId(String processDefinitionKey, String formLinkId);

    ResponseEntity<? extends FormAssociation> createFormAssociation(CreateFormAssociationRequest request);

    ResponseEntity<? extends FormAssociation> modifyFormAssociation(ModifyFormAssociationRequest request);

    ResponseEntity<Void> deleteFormAssociation(String processDefinitionKey, String formAssociationId);

}