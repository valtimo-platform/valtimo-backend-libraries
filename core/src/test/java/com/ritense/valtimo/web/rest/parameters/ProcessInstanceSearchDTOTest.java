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

package com.ritense.valtimo.web.rest.parameters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ritense.valtimo.domain.util.Jsr310DateTimeSerializer;
import com.ritense.valtimo.domain.util.Jsr310LocalDateDeserializer;
import com.ritense.valtimo.web.rest.dto.ProcessInstanceSearchDTO;
import com.ritense.valtimo.web.rest.dto.processvariable.ProcessVariableDTOV2;
import com.ritense.valtimo.web.rest.dto.processvariable.type.DateProcessVariableDTOV2;
import com.ritense.valtimo.web.rest.dto.processvariable.type.LongProcessVariableDTOV2;
import com.ritense.valtimo.web.rest.dto.processvariable.type.StringProcessVariableDTOV2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class ProcessInstanceSearchDTOTest {

    private static final String BUSINESS_KEY = "1";
    private static final Boolean ACTIVE = false;

    private static final String STRING_NAME = "some-string";
    private static final String STRING_VALUE = "some-value";

    private static final String LONG_NAME = "long-name";
    private static final Long LONG_VALUE = 2L;

    private static final String DATE_NAME = "date-name";
    private static final LocalDate DATE_FROM = LocalDate.of(2010, 1, 1);
    private static final LocalDate DATE_TO = LocalDate.of(2018, 1, 1);

    private ObjectMapper mapper;

    @BeforeEach
    void setup() {
        mapper = new ObjectMapper();
        JavaTimeModule module = new JavaTimeModule();
        module.addSerializer(OffsetDateTime.class, Jsr310DateTimeSerializer.INSTANCE);
        module.addSerializer(ZonedDateTime.class, Jsr310DateTimeSerializer.INSTANCE);
        module.addSerializer(LocalDateTime.class, Jsr310DateTimeSerializer.INSTANCE);
        module.addSerializer(Instant.class, Jsr310DateTimeSerializer.INSTANCE);
        module.addDeserializer(LocalDate.class, Jsr310LocalDateDeserializer.INSTANCE);
        mapper.registerModule(module);
    }

    @Test
    void processInstanceSearchDto() throws IOException {
        List<ProcessVariableDTOV2> processVariables = new ArrayList<>();

        LongProcessVariableDTOV2 longProcessVariableDTOV2 = new LongProcessVariableDTOV2(LONG_NAME, LONG_VALUE);

        DateProcessVariableDTOV2.DateRange dateRange = new DateProcessVariableDTOV2.DateRange(DATE_FROM, DATE_TO);
        DateProcessVariableDTOV2 dateProcessVariableDTOV2 = new DateProcessVariableDTOV2(DATE_NAME, dateRange);

        StringProcessVariableDTOV2 stringProcessVariableDTOV2 = new StringProcessVariableDTOV2(STRING_NAME, STRING_VALUE);

        processVariables.add(longProcessVariableDTOV2);
        processVariables.add(dateProcessVariableDTOV2);
        processVariables.add(stringProcessVariableDTOV2);

        ProcessInstanceSearchDTO processInstanceSearchDTO = new ProcessInstanceSearchDTO(processVariables);

        String json = mapper.writeValueAsString(processInstanceSearchDTO);

        ProcessInstanceSearchDTO processInstanceSearchDTOResult = mapper.readValue(json, ProcessInstanceSearchDTO.class);

        LongProcessVariableDTOV2 longVariable = (LongProcessVariableDTOV2) getProcessVariable(LONG_NAME, processInstanceSearchDTOResult).get();
        DateProcessVariableDTOV2 dateVariable = (DateProcessVariableDTOV2) getProcessVariable(DATE_NAME, processInstanceSearchDTOResult).get();

        assertEquals(LONG_VALUE, longVariable.getValue());
        assertEquals(DATE_FROM, dateVariable.getDateRange().getFrom());
        assertEquals(DATE_TO, dateVariable.getDateRange().getTo());
    }

    private Optional<ProcessVariableDTOV2> getProcessVariable(String name, ProcessInstanceSearchDTO processInstanceSearchDTO) {
        return processInstanceSearchDTO.getProcessVariables().stream()
            .filter(processVariableDTOV2 -> processVariableDTOV2.getName().equals(name))
            .findAny();
    }
}