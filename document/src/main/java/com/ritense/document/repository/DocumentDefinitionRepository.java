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

package com.ritense.document.repository;

import com.ritense.document.domain.DocumentDefinition;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface DocumentDefinitionRepository<T extends DocumentDefinition> extends
    JpaRepository<T, DocumentDefinition.Id> {

    Optional<T> findFirstByIdNameOrderByIdVersionDesc(String documentDefinitionName);
    Optional<T> findFirstByIdNameAndTenantIdOrderByIdVersionDesc(String documentDefinitionName, String tenantId);

    List<T> findAllByIdName(String documentDefinitionName);

    Page<T> findAll(Pageable pageable);

    Page<T> findAllByTenantId(Pageable pageable, String tenantId);

    Optional<T> findById(DocumentDefinition.Id id);

    Optional<T> findByIdAndTenantId(DocumentDefinition.Id id, String tenantId);

    Page<JsonSchemaDocumentDefinition> findAllForRoles(List<String> roles, Pageable pageable);
    Page<JsonSchemaDocumentDefinition> findAllForRolesAndTenantId(List<String> roles, String tenantId, Pageable pageable);

    void deleteByIdName(String documentDefinitionName);
}