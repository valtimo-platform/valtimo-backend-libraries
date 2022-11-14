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

import org.springframework.data.domain.Persistable;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

@Entity
@Table(name = "search_field")
public class SearchField implements Persistable<SearchFieldId> {

    @EmbeddedId
    private SearchFieldId id;

    @Column(name = "search_field_key", length = 255, nullable = false, updatable = false)
    private String key;

    @Column(name = "path", length = 255, nullable = true, updatable = true)
    private String path;

    @Column(name = "datatype", nullable = false, updatable = true)
    @Enumerated(EnumType.STRING)
    private SearchFieldDataType dataType;

    @Column(name = "fieldtype", nullable = false, updatable = true)
    @Enumerated(EnumType.STRING)
    private SearchFieldFieldType fieldType;

    @Column(name = "matchtype", nullable = true, updatable = true)
    @Enumerated(EnumType.STRING)
    private SearchFieldMatchType matchType;

    @Column(name = "search_field_order", nullable = false, updatable = true)
    private int order;

    @Column(name = "title")
    private String title;

    public SearchField(String key,
                       String path,
                       SearchFieldDataType dataType,
                       SearchFieldFieldType fieldType,
                       SearchFieldMatchType matchType,
                       int order,
                       String title) {
        this.key = key;
        this.path = path;
        this.dataType = dataType;
        this.fieldType = fieldType;
        this.matchType = matchType;
        this.order = order;
        this.title = title;
    }

    public SearchField() {}

    @Override
    public SearchFieldId getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        if ( id == null ) {
            return false;
        }

        return id.isNew();
    }

    public void setId(SearchFieldId searchFieldId) {
        this.id = searchFieldId;
    }

    public String getKey() {
        return key;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public SearchFieldDataType getDataType() {
        return dataType;
    }

    public void setDataType(SearchFieldDataType dataType) {
        this.dataType = dataType;
    }

    public SearchFieldFieldType getFieldType() {
        return fieldType;
    }

    public void setFieldType(SearchFieldFieldType fieldType) {
        this.fieldType = fieldType;
    }

    public SearchFieldMatchType getMatchType() {
        return matchType;
    }

    public void setMatchType(SearchFieldMatchType matchType) {
        this.matchType = matchType;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}


