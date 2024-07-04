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

package com.ritense.valtimo.web.rest;

import com.ritense.valtimo.domain.choicefield.ChoiceField;
import com.ritense.valtimo.domain.choicefield.ChoiceFieldValue;
import com.ritense.valtimo.service.ChoiceFieldValueService;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
class ChoiceFieldValueResourceTest {

    @Mock
    private ChoiceFieldValueService choiceFieldValueService = mock(ChoiceFieldValueService.class);

    @InjectMocks
    private ChoiceFieldValueResource choiceFieldValueResource;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(choiceFieldValueResource)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();
    }

    @Test
    void testGetChoiceFieldValues() throws Exception {

        ChoiceFieldValue choiceFieldValue = createChoiceFieldValue();

        Pageable pageable = PageRequest.of(1, 1);

        when(choiceFieldValueService.findAll(any())).thenReturn(new PageImpl<>(List.of(choiceFieldValue), pageable, 5L));

        mvc.perform(get("/api/v2/choice-field-values"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.content", hasSize(1)))
            .andExpect(jsonPath("$.content[0].id").value(choiceFieldValue.getId()))
            .andExpect(jsonPath("$.content[0].name").value(choiceFieldValue.getName()))
            .andExpect(jsonPath("$.content[0].deprecated").value(choiceFieldValue.isDeprecated()))
            .andExpect(jsonPath("$.content[0].sortOrder").value(choiceFieldValue.getSortOrder()))
            .andExpect(jsonPath("$.content[0].value").value(choiceFieldValue.getValue()))
            .andExpect(jsonPath("$.content[0].choiceField.id").value(choiceFieldValue.getChoiceField().getId()))
            .andExpect(jsonPath("$.content[0].choiceField.title").value(choiceFieldValue.getChoiceField().getTitle()))
            .andExpect(jsonPath("$.content[0].choiceField.keyName").value(choiceFieldValue.getChoiceField().getKeyName()))
            .andExpect(jsonPath("$.content[0].choiceField.createdBy").doesNotExist())
            .andExpect(jsonPath("$.content[0].choiceField.createdDate").doesNotExist())
            .andExpect(jsonPath("$.content[0].choiceField.lastModifiedBy").doesNotExist())
            .andExpect(jsonPath("$.content[0].choiceField.lastModifiedDate").doesNotExist())
            .andExpect(jsonPath("$.totalElements").value(5))
            .andExpect(jsonPath("$.totalPages").value(5));
    }

    @Test
    void testGetChoiceFieldValuesByChoiceFieldKey() throws Exception {

        ChoiceFieldValue choiceFieldValue = createChoiceFieldValue();

        Pageable pageable = PageRequest.of(1, 1);

        when(choiceFieldValueService.findAllByChoiceFieldKeyName(any(), eq("some-name"))).thenReturn(new PageImpl<>(List.of(choiceFieldValue), pageable, 5L));

        mvc.perform(get("/api/v2/choice-field-values/some-name/values"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.content", hasSize(1)))
            .andExpect(jsonPath("$.content[0].id").value(choiceFieldValue.getId()))
            .andExpect(jsonPath("$.content[0].name").value(choiceFieldValue.getName()))
            .andExpect(jsonPath("$.content[0].deprecated").value(choiceFieldValue.isDeprecated()))
            .andExpect(jsonPath("$.content[0].sortOrder").value(choiceFieldValue.getSortOrder()))
            .andExpect(jsonPath("$.content[0].value").value(choiceFieldValue.getValue()))
            .andExpect(jsonPath("$.content[0].choiceField.id").value(choiceFieldValue.getChoiceField().getId()))
            .andExpect(jsonPath("$.content[0].choiceField.title").value(choiceFieldValue.getChoiceField().getTitle()))
            .andExpect(jsonPath("$.content[0].choiceField.keyName").value(choiceFieldValue.getChoiceField().getKeyName()))
            .andExpect(jsonPath("$.content[0].choiceField.createdBy").doesNotExist())
            .andExpect(jsonPath("$.content[0].choiceField.createdDate").doesNotExist())
            .andExpect(jsonPath("$.content[0].choiceField.lastModifiedBy").doesNotExist())
            .andExpect(jsonPath("$.content[0].choiceField.lastModifiedDate").doesNotExist())
            .andExpect(jsonPath("$.totalElements").value(5))
            .andExpect(jsonPath("$.totalPages").value(5));
    }

    private static ChoiceFieldValue createChoiceFieldValue() {
        ChoiceFieldValue choiceFieldValue = new ChoiceFieldValue();
        choiceFieldValue.setId(1L);
        choiceFieldValue.setName("test");
        choiceFieldValue.setDeprecated(false);
        choiceFieldValue.setSortOrder(1L);
        choiceFieldValue.setValue("test");
        choiceFieldValue.setChoiceField(createChoiceField());
        return choiceFieldValue;
    }

    private static ChoiceField createChoiceField() {
        ChoiceField choiceField = new ChoiceField();
        choiceField.setId(1L);
        choiceField.setTitle("title");
        choiceField.setKeyName("keyName");
        choiceField.setCreatedBy("createdBy");
        choiceField.setCreatedDate(ZonedDateTime.of(2021, 1, 1, 1, 1, 1, 1, ZoneId.of("UTC")));
        choiceField.setLastModifiedBy("lastModifiedBy");
        choiceField.setLastModifiedDate(ZonedDateTime.of(2021, 1, 1, 1, 1, 1, 1, ZoneId.of("UTC")));
        return choiceField;
    }
}