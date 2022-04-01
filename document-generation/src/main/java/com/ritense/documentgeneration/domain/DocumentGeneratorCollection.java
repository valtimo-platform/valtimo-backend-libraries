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

package com.ritense.documentgeneration.domain;

import com.ritense.documentgeneration.service.DocumentGenerator;
import org.springframework.http.MediaType;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class DocumentGeneratorCollection {

    private final Map<MediaType, DocumentGenerator> collection;

    public DocumentGeneratorCollection(Map<MediaType, DocumentGenerator> collection) {
        this.collection = Collections.unmodifiableMap(collection);
    }

    public Optional<DocumentGenerator> getByMediaType(MediaType mediaType) {
        return Optional.ofNullable(collection.get(mediaType));
    }

}