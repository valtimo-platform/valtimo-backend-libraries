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
import com.ritense.valtimo.choicefield.repository.ChoiceFieldValueRepository;
import com.ritense.valtimo.domain.choicefield.ChoiceField;
import com.ritense.valtimo.domain.choicefield.ChoiceFieldValue;
import com.ritense.valtimo.web.rest.dto.ChoiceFieldValueCreateRequestDTO;
import com.ritense.valtimo.web.rest.dto.ChoiceFieldValueUpdateRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Transactional
public class ChoiceFieldValueService {

    private static final Logger logger = LoggerFactory.getLogger(ChoiceFieldValueService.class);
    private final ChoiceFieldRepository choiceFieldRepository;

    private final ChoiceFieldValueRepository choiceFieldValueRepository;

    public ChoiceFieldValueService(
        ChoiceFieldRepository choiceFieldRepository,
        ChoiceFieldValueRepository choiceFieldValueRepository
    ) {
        this.choiceFieldRepository = choiceFieldRepository;
        this.choiceFieldValueRepository = choiceFieldValueRepository;
    }

    /**
     * Save a choiceFieldValue.
     *
     * @param requestDTO the entity to save
     * @return the persisted entity
     */
    public ChoiceFieldValue create(ChoiceFieldValueCreateRequestDTO requestDTO, String choiceFieldName) {
        logger.debug("Request to create ChoiceFieldValue : {}", requestDTO);

        ChoiceField choiceField = choiceFieldRepository.findByKeyName(choiceFieldName);
        ChoiceFieldValue choiceFieldValue = new ChoiceFieldValue();
        choiceFieldValue.setName(requestDTO.getName());
        choiceFieldValue.setDeprecated(requestDTO.getDeprecated());
        choiceFieldValue.setSortOrder(requestDTO.getSortOrder());
        choiceFieldValue.setValue(requestDTO.getValue());
        choiceFieldValue.setChoiceField(choiceField);

        return choiceFieldValueRepository.save(choiceFieldValue);
    }

    public ChoiceFieldValue update(ChoiceFieldValueUpdateRequestDTO requestDTO, String choiceFieldName) {
        logger.debug("Request to update ChoiceFieldValue : {}", requestDTO);

        ChoiceField choiceField = choiceFieldRepository.findByKeyName(choiceFieldName);

        ChoiceFieldValue choiceFieldValue = choiceFieldValueRepository.findById(requestDTO.getId()).orElseThrow();
        choiceFieldValue.setName(requestDTO.getName());
        choiceFieldValue.setDeprecated(requestDTO.getDeprecated());
        choiceFieldValue.setSortOrder(requestDTO.getSortOrder());
        choiceFieldValue.setValue(requestDTO.getValue());
        choiceFieldValue.setChoiceField(choiceField);

        return choiceFieldValueRepository.save(choiceFieldValue);
    }

    /**
     * Get all the choiceFieldValues.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<ChoiceFieldValue> findAll(Pageable pageable) {
        logger.debug("Request to get all ChoiceFieldValues");
        return choiceFieldValueRepository.findAll(pageable);
    }

    /**
     * Get all the choiceFieldValues.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<ChoiceFieldValue> findAllByChoiceFieldId(Pageable pageable, Long choicefieldId) {
        logger.debug("Request to get all ChoiceFieldValues for choiceField with id {}", choicefieldId);
        return choiceFieldValueRepository.findByChoiceField_Id(pageable, choicefieldId);
    }


    /**
     * Get all the choiceFieldValues.
     *
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<ChoiceFieldValue> findAllByChoiceFieldIdAndDeprecatedIsFalse(Long choicefieldId) {
        logger.debug("Request to get not-deprecated ChoiceFieldValues for choiceField witd id {}", choicefieldId);
        return choiceFieldValueRepository.findByChoiceField_IdAndDeprecatedIsFalse(choicefieldId);
    }

    /**
     * Get one choiceFieldValue by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public Optional<ChoiceFieldValue> findOne(Long id) {
        logger.debug("Request to get ChoiceFieldValue : {}", id);
        return choiceFieldValueRepository.findById(id);
    }

    /**
     * Delete the  choiceFieldValue by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        logger.debug("Request to delete ChoiceFieldValue : {}", id);
        choiceFieldValueRepository.deleteById(id);
    }

    /**
     * Get all the choiceFieldValues.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<ChoiceFieldValue> findAllByChoiceFieldKeyName(Pageable pageable, String choiceFieldName) {
        logger.debug("Request to get all ChoiceFieldValues for choiceField witd name {}", choiceFieldName);
        return choiceFieldValueRepository.findByChoiceField_KeyName(pageable, choiceFieldName);
    }

    /**
     * Get all the choiceFieldValues.
     *
     * @param choiceFieldName the name of the choicefield
     * @param value           the value of the choicefield value
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public ChoiceFieldValue findOneByChoiceFieldNameAndValue(String choiceFieldName, String value) {
        logger.debug("Request to get all ChoiceFieldValues for choiceField {} and value {}", choiceFieldName, value);
        return choiceFieldValueRepository.findTop1ByChoiceField_KeyNameAndValue(choiceFieldName, value);
    }
}
