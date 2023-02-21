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

package com.ritense.processdocument.domain.impl;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "document_definition_process_link")
public class DocumentDefinitionProcessLink {

    @EmbeddedId
    private DocumentDefinitionProcessLinkId id;

    @Column(name = "link_type", nullable = false)
    private String type;

    public DocumentDefinitionProcessLink(
        DocumentDefinitionProcessLinkId documentDefinitionProcessLinkId,
        String type
    ) {
        this.id = documentDefinitionProcessLinkId;
        this.type = type;
    }

    public DocumentDefinitionProcessLink() {
    }

    public DocumentDefinitionProcessLinkId getId() {
        return id;
    }

    public String getType() {
        return type;
    }
}
