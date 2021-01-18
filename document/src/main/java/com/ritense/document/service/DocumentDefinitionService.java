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

package com.ritense.document.service;

import com.ritense.document.domain.DocumentDefinition;
import com.ritense.document.domain.impl.JsonSchema;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinitionId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface DocumentDefinitionService {

    Page<? extends DocumentDefinition> findAll(Pageable pageable);

    JsonSchemaDocumentDefinitionId findIdByName(String name);

    Optional<? extends DocumentDefinition> findBy(DocumentDefinition.Id id);

    Optional<? extends DocumentDefinition> findLatestByName(String documentDefinitionName);

    void deployAll();

    void deploy(JsonSchema schema);

    void deploy(JsonSchemaDocumentDefinition documentDefinition);

}