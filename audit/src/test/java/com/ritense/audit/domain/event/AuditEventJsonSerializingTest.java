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

package com.ritense.audit.domain.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ritense.audit.AbstractTestHelper;
import com.ritense.valtimo.contract.json.Mapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class AuditEventJsonSerializingTest extends AbstractTestHelper {

    private static final String dateString = "2019-03-18T14:17:11.639114";
    private JacksonTester<TestEvent> jacksonTester;
    private final String id = "edb1a672-4ba1-4e79-a5ee-b9658c55fe52";
    private String jsonString;

    private ObjectMapper objectMapper = Mapper.INSTANCE.get();

    @BeforeEach
    public void setUp() throws IOException {
        JacksonTester.initFields(this, objectMapper);
        jsonString = TestHelper.getResourceAsString("json/event/TestEvent.json");
    }

    @Test
    public void shouldParseJson() throws IOException {
        final TestEvent testEvent = testEvent(id, LocalDateTime.parse(dateString));
        assertThat(this.jacksonTester.parse(jsonString)).isEqualTo(testEvent);
    }

    @Test
    public void shouldMarshalObjectToJson() throws IOException {
        final TestEvent testEvent = testEvent(id, LocalDateTime.parse(dateString));
        assertThat(this.jacksonTester.write(testEvent)).isEqualTo(jsonString);
    }

}