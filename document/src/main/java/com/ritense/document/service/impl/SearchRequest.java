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

package com.ritense.document.service.impl;

import java.util.List;
import java.util.Objects;

public class SearchRequest {
    private String documentDefinitionName;
    private String createdBy;
    private String globalSearchFilter;
    private Long sequence;
    private List<SearchCriteria> otherFilters;
    private String tenantId;

    public SearchRequest() {
    }

    public String getDocumentDefinitionName() {
        return this.documentDefinitionName;
    }

    public String getCreatedBy() {
        return this.createdBy;
    }

    public String getGlobalSearchFilter() {
        return this.globalSearchFilter;
    }

    public Long getSequence() {
        return this.sequence;
    }

    public List<SearchCriteria> getOtherFilters() {
        return this.otherFilters;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setDocumentDefinitionName(String documentDefinitionName) {
        this.documentDefinitionName = documentDefinitionName;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public void setGlobalSearchFilter(String globalSearchFilter) {
        this.globalSearchFilter = globalSearchFilter;
    }

    public void setSequence(Long sequence) {
        this.sequence = sequence;
    }

    public void setOtherFilters(List<SearchCriteria> otherFilters) {
        this.otherFilters = otherFilters;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SearchRequest that = (SearchRequest) o;
        return Objects.equals(getDocumentDefinitionName(), that.getDocumentDefinitionName()) && Objects.equals(getCreatedBy(), that.getCreatedBy()) && Objects.equals(getGlobalSearchFilter(), that.getGlobalSearchFilter()) && Objects.equals(getSequence(), that.getSequence()) && Objects.equals(getOtherFilters(), that.getOtherFilters());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDocumentDefinitionName(), getCreatedBy(), getGlobalSearchFilter(), getSequence(), getOtherFilters());
    }

    @Override
    public String toString() {
        return "SearchRequest{" +
            "documentDefinitionName='" + documentDefinitionName + '\'' +
            ", createdBy='" + createdBy + '\'' +
            ", globalSearchFilter='" + globalSearchFilter + '\'' +
            ", sequence=" + sequence +
            ", otherFilters=" + otherFilters +
            '}';
    }
}
