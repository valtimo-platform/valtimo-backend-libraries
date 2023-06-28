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

package com.ritense.valtimo.repository.camunda.dto;

import com.ritense.valtimo.camunda.domain.CamundaProcessDefinition;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InstanceCountChart {
    private List<String> categories = new ArrayList<>();
    private Map<String, ChartInstanceSeries> series = new HashMap<>();
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM");

    public InstanceCountChart(List<CamundaProcessDefinition> processDefinitions, List<InstanceCount> processInstances) {
        // Set instance count in map
        Map<String, Map<String, Long>> instanceCountPerProces = new HashMap<>();
        for (InstanceCount instanceCount : processInstances) {
            Map<String, Long> newSeriesData = new HashMap<>();
            for (InstanceCountValue instanceCountValue :instanceCount.getValues()) {
                newSeriesData.put(simpleDateFormat.format(instanceCountValue.getDate()), instanceCountValue.getCount());
            }
            instanceCountPerProces.put(instanceCount.getName(), newSeriesData);
        }
        // Add categories last 14 days
        Calendar daysInPast = Calendar.getInstance();
        daysInPast.add(Calendar.DAY_OF_YEAR, -14);
        for (int i = 0; i <= 14; ++i) {
            String dayToProcess = simpleDateFormat.format(daysInPast.getTime());
            this.categories.add(dayToProcess);

            // Add series data for every deployed process
            for (CamundaProcessDefinition processDefinition :processDefinitions) {
                Map<String, Long> dateCounts = instanceCountPerProces.get(processDefinition.getName());
                Long count = dateCounts != null && dateCounts.get(dayToProcess) != null ? dateCounts.get(dayToProcess) : 0L;
                this.addSeriesValue(processDefinition.getName(), count);
            }
            daysInPast.add(Calendar.DAY_OF_YEAR, 1);
        }

    }

    public List<String> getCategories() {
        return this.categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public Collection<ChartInstanceSeries> getSeries() {
        return this.series.values();
    }

    public void addSeriesValue(String key, long count) {
        if (!this.series.containsKey(key)) {
            ChartInstanceSeries processInstanceCountSeries = new ChartInstanceSeries();
            processInstanceCountSeries.setName(key);
            this.series.put(key, processInstanceCountSeries);
        }

        this.series.get(key).getData().add(Long.valueOf(count));
    }
}
