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

package com.ritense.document.service;

import com.ritense.document.domain.Document;
import com.ritense.document.domain.search.AdvancedSearchRequest;
import com.ritense.document.domain.search.SearchWithConfigRequest;
import com.ritense.document.service.impl.SearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DocumentSearchService<T extends Document> {

    @SuppressWarnings({"squid:S1452","java:S1452"})
    Page<T> search(
        SearchRequest searchRequest,
        Pageable pageable
    );

    @SuppressWarnings({"squid:S1452","java:S1452"})
    Page<T> search(String documentDefinitionName, SearchWithConfigRequest searchWithConfigRequest, Pageable pageable);

    @SuppressWarnings({"squid:S1452","java:S1452"})
    Page<T> search(String documentDefinitionName, AdvancedSearchRequest searchRequest, Pageable pageable);

    Long count(String documentDefinitionName, AdvancedSearchRequest advancedSearchRequest);

}
