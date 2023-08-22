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

    @Query(nativeQuery = true, value = "" +
        "select " +
        "    distinct jsonschema.document_definition_name, " +
        "    jsonschema.document_definition_version, " +
        "    jsonschema.created_on, " +
        "    jsonschema.read_only, " +
        "    jsonschema.json_schema " +
        "from " +
        "    json_schema_document_definition jsonschema " +
        "inner join " +
        "    json_schema_document_definition_role jsonschema_role " +
        "        on (" +
        "            jsonschema_role.document_definition_name = jsonschema.document_definition_name " +
        "            and (" +
        "                jsonschema_role.role in (:roles)" +
        "            )" +
        "        ) " +
        "inner join (" +
        "        select" +
        "            max(jsonschema_for_max.document_definition_version) as latest_version," +
        "            jsonschema_for_max.document_definition_name" +
        "        from" +
        "            json_schema_document_definition jsonschema_for_max " +
        "        group by " +
        "            jsonschema_for_max.document_definition_name" +
        ") version_per_definition " +
        "on version_per_definition.latest_version = jsonschema.document_definition_version " +
        "and version_per_definition.document_definition_name = jsonschema.document_definition_name ")
    Page<JsonSchemaDocumentDefinition> findAllForRoles(List<String> roles, Pageable pageable);

    @Query(nativeQuery = true, value = "" +
        "select " +
        "    distinct jsonschema.document_definition_name, " +
        "    jsonschema.document_definition_version, " +
        "    jsonschema.created_on, " +
        "    jsonschema.read_only, " +
        "    jsonschema.json_schema " +
        "from " +
        "    json_schema_document_definition jsonschema " +
        "inner join (" +
        "        select" +
        "            max(jsonschema_for_max.document_definition_version) as latest_version," +
        "            jsonschema_for_max.document_definition_name" +
        "        from" +
        "            json_schema_document_definition jsonschema_for_max " +
        "        group by " +
        "            jsonschema_for_max.document_definition_name" +
        ") version_per_definition " +
        "on version_per_definition.latest_version = jsonschema.document_definition_version " +
        "and version_per_definition.document_definition_name = jsonschema.document_definition_name ")
    Page<JsonSchemaDocumentDefinition> findAll(Pageable pageable);
}