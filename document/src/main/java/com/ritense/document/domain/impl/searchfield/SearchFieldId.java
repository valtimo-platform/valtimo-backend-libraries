/*
 *  Copyright 2015-2022 Ritense BV, the Netherlands.
 *
 *  Licensed under EUPL, Version 1.2 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.ritense.document.domain.impl.searchfield;

import com.ritense.valtimo.contract.domain.AbstractId;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class SearchFieldId extends AbstractId<SearchFieldId> {

    @Column(name = "search_field_id", columnDefinition = "VARCHAR(50)", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "document_definition_name", columnDefinition = "VARCHAR(50)", nullable = false, updatable = false)
    private String documentDefinitionName;

    private SearchFieldId(UUID id, String documentDefinitionName) {
        this.id = id;
        this.documentDefinitionName = documentDefinitionName;
    }

    public SearchFieldId() {}

    public static SearchFieldId newId(String documentDefinitionName) {
        return new SearchFieldId(UUID.randomUUID(), documentDefinitionName);
    }

    public String getDocumentDefinitionName() {
        return this.documentDefinitionName;
    }

    public UUID getId() {
        return this.id;
    }
}
