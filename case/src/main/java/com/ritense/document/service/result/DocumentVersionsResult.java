/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

package com.ritense.document.service.result;

import com.ritense.valtimo.contract.case_.CaseDefinitionId;
import java.util.List;

public class DocumentVersionsResult {
    private String name;
    private List<CaseDefinitionId> versions;

    public DocumentVersionsResult(String name, List<CaseDefinitionId> versions) {
        this.name = name;
        this.versions = versions;
    }

    public String getName() {
        return name;
    }

    public List<CaseDefinitionId> getVersions() {
        return versions;
    }
}
