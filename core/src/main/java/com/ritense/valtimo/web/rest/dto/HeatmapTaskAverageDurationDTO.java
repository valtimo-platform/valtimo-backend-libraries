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

package com.ritense.valtimo.web.rest.dto;

public class HeatmapTaskAverageDurationDTO extends HeatmapTaskDTO {
    private long averageDurationInMilliseconds;

    public HeatmapTaskAverageDurationDTO(String name, long count, long totalCount, long averageDurationInMilliseconds) {
        super(name, count, totalCount);
        this.averageDurationInMilliseconds = averageDurationInMilliseconds;
    }

    public HeatmapTaskAverageDurationDTO(String name) {
        super(name, 0L, 0L);
        averageDurationInMilliseconds = 0;
    }

    public long getAverageDurationInMilliseconds() {
        return averageDurationInMilliseconds;
    }

    public void setAverageDurationInMilliseconds(long averageDurationInMilliseconds) {
        this.averageDurationInMilliseconds = averageDurationInMilliseconds;
    }
}
