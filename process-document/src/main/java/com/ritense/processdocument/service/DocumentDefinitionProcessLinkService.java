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

package com.ritense.processdocument.service;

import com.ritense.processdocument.domain.impl.DocumentDefinitionProcess;
import com.ritense.processdocument.domain.impl.DocumentDefinitionProcessLink;
import com.ritense.processdocument.domain.impl.request.DocumentDefinitionProcessLinkResponse;
import com.ritense.processdocument.domain.impl.request.DocumentDefinitionProcessRequest;

import java.util.List;
import java.util.Optional;

public interface DocumentDefinitionProcessLinkService {

    /**
     * @deprecated - This method will be removed in the future
     * Use {@link #getDocumentDefinitionProcessList(String)} instead.
     */
    @Deprecated(forRemoval = true, since = "9.22.0")
    DocumentDefinitionProcess getDocumentDefinitionProcess(String documentDefinitionName);

    /**
     * @deprecated - This method will be removed in the future
     * Use {@link #getDocumentDefinitionProcessLink(String, String)} instead.
     */
    @Deprecated(forRemoval = true, since = "9.22.0")
    Optional<DocumentDefinitionProcessLink> getDocumentDefinitionProcessLink(String documentDefinitionName);

    List<DocumentDefinitionProcess> getDocumentDefinitionProcessList(String documentDefinitionName);

    Optional<DocumentDefinitionProcessLink> getDocumentDefinitionProcessLink(String documentDefinitionName, String type);

    DocumentDefinitionProcessLinkResponse saveDocumentDefinitionProcess(String documentDefinitionName, DocumentDefinitionProcessRequest request);

    void deleteDocumentDefinitionProcess(String documentDefinitionName);
}
