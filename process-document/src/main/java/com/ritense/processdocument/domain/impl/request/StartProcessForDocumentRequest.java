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

package com.ritense.processdocument.domain.impl.request;

import com.ritense.document.domain.Document;
import com.ritense.processdocument.domain.request.Request;
import java.util.Map;
import lombok.Data;

@Data
public class StartProcessForDocumentRequest implements Request {
    private final Document.Id documentId;
    private final String processDefinitionKey;
    private final Map<String, Object> processVars;
}