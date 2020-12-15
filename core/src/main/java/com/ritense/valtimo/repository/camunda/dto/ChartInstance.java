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

package com.ritense.valtimo.repository.camunda.dto;

import java.util.List;
import java.util.Map;

/**
 * Created by Paulo Lobao on 25-8-2016.
 */
public class ChartInstance {
    private List<String> categories;
    private Map<String, ChartInstanceSeries> series;

    public ChartInstance(List<String> categories, Map<String, ChartInstanceSeries> series) {
        this.categories = categories;
        this.series = series;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public Map<String, ChartInstanceSeries> getSeries() {
        return series;
    }

    public void setSeries(Map<String, ChartInstanceSeries> series) {
        this.series = series;
    }
}
