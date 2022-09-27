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

package com.ritense.processdocument.repository;

import com.ritense.processdocument.domain.impl.DocumentDefinitionProcessLink;
import com.ritense.processdocument.domain.impl.DocumentDefinitionProcessLinkId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentDefinitionProcessLinkRepository
    extends JpaRepository<DocumentDefinitionProcessLink, DocumentDefinitionProcessLinkId> {

    Optional<DocumentDefinitionProcessLink> findByIdDocumentDefinitionNameAndType(String documentDefinitionName, String type);

    List<DocumentDefinitionProcessLink> findAllByIdDocumentDefinitionName(String documentDefinitionName);

    void deleteByIdDocumentDefinitionName(String documentDefinitionName);
}
