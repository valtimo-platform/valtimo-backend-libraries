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
    private SearchFieldDatatype datatype;
    private SearchFieldFieldtype fieldtype;
    private SearchFieldMatchtype matchtype;

    public SearchFieldDto(String key,
                          String path,
                          SearchFieldDatatype datatype,
                          SearchFieldFieldtype fieldtype,
                          SearchFieldMatchtype matchtype) {
        this.key = key;
        this.path = path;
        this.datatype = datatype;
        this.fieldtype = fieldtype;
        this.matchtype = matchtype;
    }

    public SearchFieldDto() {}

    public String getKey() {
        return key;
    }

    public String getPath() {
        return path;
    }

    public SearchFieldDatatype getDatatype() {
        return datatype;
    }

    public SearchFieldFieldtype getFieldtype() {
        return fieldtype;
    }

    public SearchFieldMatchtype getMatchtype() {
        return matchtype;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setDatatype(SearchFieldDatatype datatype) {
        this.datatype = datatype;
    }

    public void setFieldtype(SearchFieldFieldtype fieldtype) {
        this.fieldtype = fieldtype;
    }

    public void setMatchtype(SearchFieldMatchtype matchtype) {
        this.matchtype = matchtype;
    }
}


