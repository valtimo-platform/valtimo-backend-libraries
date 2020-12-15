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

package com.ritense.valtimo.service.dto;

import java.util.Map;

@Deprecated
public class ProcessSearchPropertyDTO {
    public static final String SEPERATOR = ",";
    public static final String SEARCH_VARIABLES = "searchVariables";
    public static final String LIST_VARIABLES = "listVariables";

    private String[] searchVariables;
    private String[] listVariables;

    public ProcessSearchPropertyDTO(Map<String, String> properties) {
        this.listVariables = properties.get(LIST_VARIABLES) != null ? properties.get(LIST_VARIABLES).split(SEPERATOR) : null;
        this.searchVariables = properties.get(SEARCH_VARIABLES) != null ? properties.get(SEARCH_VARIABLES).split(SEPERATOR) : null;
    }

    public String[] getSearchVariables() {
        return searchVariables;
    }

    public String[] getListVariables() {
        return listVariables;
    }
}