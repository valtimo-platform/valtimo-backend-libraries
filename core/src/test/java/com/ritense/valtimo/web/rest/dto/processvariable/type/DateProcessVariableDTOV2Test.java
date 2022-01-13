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

package com.ritense.valtimo.web.rest.dto.processvariable.type;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DateProcessVariableDTOV2Test {
    private static final String NAME = "some-name";
    private static final LocalDate FROM = LocalDate.of(2010, 1, 1);
    private static final LocalDate TO = LocalDate.of(2017, 1, 1);

    @Test
    void dateProcessVariableDTOV2() {
        DateProcessVariableDTOV2.DateRange dateRange = new DateProcessVariableDTOV2.DateRange(FROM, TO);
        DateProcessVariableDTOV2 dateProcessVariableDTOV2 = new DateProcessVariableDTOV2(NAME, dateRange);

        assertEquals(NAME, dateProcessVariableDTOV2.getName());
        assertEquals(FROM, dateProcessVariableDTOV2.getDateRange().getFrom());
        assertEquals(TO, dateProcessVariableDTOV2.getDateRange().getTo());
    }

    @Test
    void dateProcessVariableDTOV2DateRangeNull() {
        assertThrows(NullPointerException.class, () -> new DateProcessVariableDTOV2(NAME, null));
    }

    @Test
    void dateProcessVariableDTOV2NameNull() {
        DateProcessVariableDTOV2.DateRange dateRange = new DateProcessVariableDTOV2.DateRange(FROM, TO);
        assertThrows(NullPointerException.class, () -> new DateProcessVariableDTOV2(null, dateRange));
    }
}