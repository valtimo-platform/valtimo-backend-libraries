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

package com.ritense.valtimo.camunda.task.domain.reminder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotEmpty;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotNull;

public class Task {

    private final String id;
    private final String name;
    private final String date;

    public Task(String id, String name, LocalDate creationTime) {
        assertArgumentNotEmpty(id, "taskId is required");
        assertArgumentNotEmpty(name, "name is required");
        assertArgumentNotNull(creationTime, "creationTime is required");
        this.id = id;
        this.name = name;
        this.date = creationTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

}