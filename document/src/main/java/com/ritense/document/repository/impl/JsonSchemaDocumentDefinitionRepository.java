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

package com.ritense.document.repository.impl;

import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition;
import com.ritense.document.repository.DocumentDefinitionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JsonSchemaDocumentDefinitionRepository extends DocumentDefinitionRepository<JsonSchemaDocumentDefinition> {

    @Query(""
        + "SELECT  distinct dd "
        + "FROM    JsonSchemaDocumentDefinition dd "
        + "INNER JOIN JsonSchemaDocumentDefinitionRole ddRole ON ddRole.id.documentDefinitionName = dd.id.name AND ddRole.id.role in :roles "
        + "WHERE   dd.id.version = (" +
        "   SELECT max(dd2.id.version) " +
        "   FROM JsonSchemaDocumentDefinition dd2 " +
        "   WHERE dd2.id.name = dd.id.name " +
        ") ")
    Page<JsonSchemaDocumentDefinition> findAllForRoles(List<String> roles, Pageable pageable);

    @Query(""
        + "SELECT  dd "
        + "FROM    JsonSchemaDocumentDefinition dd "
        + "WHERE   dd.id.version = (" +
        "   SELECT max(dd2.id.version) " +
        "   FROM JsonSchemaDocumentDefinition dd2 " +
        "   WHERE dd2.id.name = dd.id.name " +
        ") ")
    Page<JsonSchemaDocumentDefinition> findAll(Pageable pageable);
}