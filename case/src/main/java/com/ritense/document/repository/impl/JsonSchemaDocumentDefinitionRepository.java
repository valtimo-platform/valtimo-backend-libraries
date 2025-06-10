/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

package com.ritense.document.repository.impl;

import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition;
import com.ritense.document.repository.DocumentDefinitionRepository;
import com.ritense.valtimo.contract.case_.CaseDefinitionId;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface JsonSchemaDocumentDefinitionRepository extends DocumentDefinitionRepository<JsonSchemaDocumentDefinition> {

    @Query("" +
        "   SELECT      definition.id.caseDefinitionId" +
        "   FROM        JsonSchemaDocumentDefinition definition " +
        "   WHERE       definition.id.name = :documentDefinitionName " +
        "   ORDER BY    definition.id.caseDefinitionId.key, definition.id.caseDefinitionId.versionTag DESC")
    List<CaseDefinitionId> findVersionsByName(String documentDefinitionName);
}