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
import com.ritense.valtimo.service.ChoiceFieldService;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
class ChoiceFieldResourceTest {

    @Mock
    private ChoiceFieldService choiceFieldService = mock(ChoiceFieldService.class);

    @InjectMocks
    private ChoiceFieldResource choiceFieldResource;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(choiceFieldResource)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();
    }

    @Test
    void testGetChoiceFields() throws Exception {

        ChoiceField choiceField = createChoiceField();

        Pageable pageable = PageRequest.of(1, 1);

        when(choiceFieldService.findAll(any())).thenReturn(new PageImpl<>(List.of(choiceField), pageable, 5L));

        mvc.perform(get("/api/v2/choice-fields"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.content", hasSize(1)))
            .andExpect(jsonPath("$.content[0].id").value(choiceField.getId().intValue()))
            .andExpect(jsonPath("$.content[0].title").value(choiceField.getTitle()))
            .andExpect(jsonPath("$.content[0].keyName").value(choiceField.getKeyName()))
            .andExpect(jsonPath("$.content[0].createdBy").doesNotExist())
            .andExpect(jsonPath("$.content[0].createdDate").doesNotExist())
            .andExpect(jsonPath("$.content[0].lastModifiedBy").doesNotExist())
            .andExpect(jsonPath("$.content[0].lastModifiedDate").doesNotExist())
            .andExpect(jsonPath("$.totalElements").value(5))
            .andExpect(jsonPath("$.totalPages").value(5));
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