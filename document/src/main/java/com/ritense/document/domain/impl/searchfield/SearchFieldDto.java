/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

package com.ritense.document.domain.impl.searchfield;

public class SearchFieldDto {


    private String key;
    private String path;
    private SearchFieldDataType dataType;
    private SearchFieldFieldType fieldType;
    private SearchFieldMatchType matchType;
    private String title;

    public SearchFieldDto(String key,
                          String path,
                          SearchFieldDataType dataType,
                          SearchFieldFieldType fieldType,
                          SearchFieldMatchType matchType,
                          String title) {
        this.key = key;
        this.path = path;
        this.dataType = dataType;
        this.fieldType = fieldType;
        this.matchType = matchType;
        this.title = title;
    }

    public SearchFieldDto() {}

    public String getKey() {
        return key;
    }

    public String getPath() {
        return path;
    }

    public SearchFieldDataType getDataType() {
        return dataType;
    }

    public SearchFieldFieldType getFieldType() {
        return fieldType;
    }

    public SearchFieldMatchType getMatchType() {
        return matchType;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setDataType(SearchFieldDataType dataType) {
        this.dataType = dataType;
    }

    public void setFieldType(SearchFieldFieldType fieldType) {
        this.fieldType = fieldType;
    }

    public void setMatchType(SearchFieldMatchType matchType) {
        this.matchType = matchType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}


