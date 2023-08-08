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

package com.ritense.document.repository;

import com.ritense.document.domain.impl.searchfield.SearchField;
import com.ritense.document.domain.impl.searchfield.SearchFieldId;
import java.util.List;
import java.util.Optional;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SearchFieldRepository extends JpaRepository<SearchField, SearchFieldId>, JpaSpecificationExecutor<SearchField> {

    List<SearchField> findAllByIdDocumentDefinitionNameOrderByOrder(String documentDefinitionName);

    Optional<SearchField> findByIdDocumentDefinitionNameAndKey(String documentDefinitionName, String key);

    boolean existsByIdDocumentDefinitionName(String documentDefinitionName);

    void deleteAllByIdDocumentDefinitionName(String documentDefinitionName);

    static Specification<SearchField> byIdDocumentDefinitionName(String documentDefinitionName){
        return new Specification<SearchField>() {
            @Override
            public Predicate toPredicate(Root<SearchField> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                return criteriaBuilder.equal(root.get("id").get("documentDefinitionName"), documentDefinitionName);
            }
        };
    }
}
