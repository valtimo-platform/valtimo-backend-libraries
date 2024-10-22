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

package com.ritense.valtimo.web.rest;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ritense.valtimo.BaseIntegrationTest;
import com.ritense.valtimo.choicefield.repository.ChoiceFieldRepository;
import com.ritense.valtimo.choicefield.repository.ChoiceFieldValueRepository;
import com.ritense.valtimo.domain.choicefield.ChoiceField;
import com.ritense.valtimo.domain.choicefield.ChoiceFieldValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@Transactional
class ChoiceFieldValueResourceIntTest extends BaseIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ChoiceFieldRepository choiceFieldRepository;

    @Autowired
    private ChoiceFieldValueRepository choiceFieldValueRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void init() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void shoudlRetrieveChoicefieldValues() throws Exception {
        ChoiceField choiceField = new ChoiceField();
        choiceField.setKeyName("keyName");
        choiceField.setTitle("title");
        choiceFieldRepository.save(choiceField);

        ChoiceFieldValue choiceFieldValue = new ChoiceFieldValue();
        choiceFieldValue.setChoiceField(choiceField);
        choiceFieldValue.setValue("value");
        choiceFieldValue.setName("name");
        choiceFieldValue.setDeprecated(false);
        choiceFieldValueRepository.save(choiceFieldValue);

        mockMvc.perform(
            get("/api/v1/choice-field-values/{choice_field_name}/values", "keyName")
                .accept(APPLICATION_JSON_VALUE)
        )
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").isNumber())
        .andExpect(jsonPath("$[0].value").value("value"))
        .andExpect(jsonPath("$[0].deprecated").value(false))
        .andExpect(jsonPath("$[0].name").value("name"))
        .andExpect(jsonPath("$[0].choiceField.id").isNumber())
        .andExpect(jsonPath("$[0].choiceField.keyName").value("keyName"))
        .andExpect(jsonPath("$[0].choiceField.title").value("title"));
    }
}