/*
 * Copyright 2015-2025 Ritense BV, the Netherlands.
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

package com.ritense.document.domain.impl;

@SuppressWarnings({"squid:S1206", "java:S1206"})
public class DocumentContentFilter {
    private static boolean includeDocumentContent = false;

    public static void setIncludeDocumentContent(boolean value) {
        includeDocumentContent = value;
    }

    @Override
    public boolean equals(Object obj) {
        // Return true if the field should be considered "filtered out"
        return !includeDocumentContent;
    }
}