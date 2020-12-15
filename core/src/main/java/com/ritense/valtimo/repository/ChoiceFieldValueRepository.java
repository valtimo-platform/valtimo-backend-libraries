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

package com.ritense.valtimo.repository;

import com.ritense.valtimo.domain.choicefield.ChoiceFieldValue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for the ChoiceFieldValue entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ChoiceFieldValueRepository extends JpaRepository<ChoiceFieldValue, Long> {
    Page<ChoiceFieldValue> findByChoiceField_Id(Pageable pageable, @Param("choice_field_id") Long choiceFieldId);

    Page<ChoiceFieldValue> findByChoiceField_KeyName(Pageable pageable, @Param("choice_field_name") String choiceFieldName);

    ChoiceFieldValue findTop1ByChoiceField_KeyNameAndValue(@Param("key_name") String choiceFieldName, @Param("value") String value);

    List<ChoiceFieldValue> findByChoiceField_IdAndDeprecatedIsFalse(@Param("choice_field_id") Long choiceFieldId);
}
