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

package com.ritense.valtimo.service;

import com.ritense.valtimo.choicefield.repository.ChoiceFieldRepository;
import com.ritense.valtimo.domain.choicefield.ChoiceField;
import com.ritense.valtimo.domain.choicefield.ChoiceFieldValue;
import com.ritense.valtimo.web.rest.dto.ChoiceFieldDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Transactional
public class ChoiceFieldService {

    private static final Logger logger = LoggerFactory.getLogger(ChoiceFieldService.class);
    private final ChoiceFieldRepository choiceFieldRepository;
    private final ChoiceFieldValueService choiceFieldValueService;

    public ChoiceFieldService(ChoiceFieldRepository choiceFieldRepository, ChoiceFieldValueService choiceFieldValueService) {
        this.choiceFieldRepository = choiceFieldRepository;
        this.choiceFieldValueService = choiceFieldValueService;
    }

    /**
     * Save a choiceField.
     *
     * @param choiceField the entity to save
     * @return the persisted entity
     */
    public ChoiceField save(ChoiceField choiceField) {
        logger.debug("Request to save ChoiceField : {}", choiceField);
        ChoiceField result = choiceFieldRepository.save(choiceField);
        return result;
    }

    /**
     * Get all the choiceFields.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<ChoiceField> findAll(Pageable pageable) {
        logger.debug("Request to get all ChoiceFields");
        Page<ChoiceField> result = choiceFieldRepository.findAll(pageable);
        return result;
    }

    /**
     * Get one choiceField by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public Optional<ChoiceField> findOneById(Long id) {
        logger.debug("Request to get ChoiceField : {}", id);
        Optional<ChoiceField> choiceField = choiceFieldRepository.findById(id);
        return choiceField;
    }


    /**
     * Get one choiceField by name.
     *
     * @param name the name of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public ChoiceFieldDTO findOneByName(String name) {
        logger.debug("Request to get ChoiceField : {}", name);

        ChoiceField choiceField = choiceFieldRepository.findByKeyName(name);

        if (choiceField == null) {
            return null;
        }

        List<ChoiceFieldValue> choiceFieldValues = choiceFieldValueService.findAllByChoiceFieldIdAndDeprecatedIsFalse(choiceField.getId());

        ChoiceFieldDTO choiceFieldDTO = new ChoiceFieldDTO();
        choiceFieldDTO.setId(choiceField.getId());
        choiceFieldDTO.setKeyName(choiceField.getKeyName());
        if (choiceFieldValues != null) {
            choiceFieldDTO.setChoiceFieldValues(choiceFieldValues.stream()
                .sorted(Comparator.comparing(ChoiceFieldValue::getSortOrder)).collect(Collectors.toList()));
        }

        return choiceFieldDTO;
    }

    /**
     * Delete the  choiceField by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        logger.debug("Request to delete ChoiceField : {}", id);
        choiceFieldRepository.deleteById(id);
    }
}
