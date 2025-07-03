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

package com.ritense.document.repository;

import com.ritense.document.domain.impl.searchfield.SearchField;
import com.ritense.document.domain.impl.searchfield.SearchFieldId;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SearchFieldRepository extends JpaRepository<SearchField, SearchFieldId>, JpaSpecificationExecutor<SearchField> {

    List<SearchField> findAllByIdCaseDefinitionKeyOrderByOrder(String caseDefinitionKey);

    Optional<SearchField> findByIdCaseDefinitionKeyAndKey(String caseDefinitionKey, String key);

    boolean existsByIdCaseDefinitionKey(String caseDefinitionKey);

    void deleteAllByIdCaseDefinitionKey(String caseDefinitionKey);

    static Specification<SearchField> byIdCaseDefinitionKey(String caseDefinitionKey) {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.equal(root.get("id").get("caseDefinitionKey"), caseDefinitionKey);
    }
}
