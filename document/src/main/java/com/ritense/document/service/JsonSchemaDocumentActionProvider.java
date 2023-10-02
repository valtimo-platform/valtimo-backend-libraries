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

import com.ritense.authorization.Action;
import com.ritense.authorization.ResourceActionProvider;
import com.ritense.document.domain.impl.JsonSchemaDocument;
import org.jetbrains.annotations.NotNull;
import java.util.List;

public class JsonSchemaDocumentActionProvider implements ResourceActionProvider<JsonSchemaDocument> {

    public static Action<JsonSchemaDocument> VIEW = new Action<>(Action.VIEW);
    public static Action<JsonSchemaDocument> VIEW_LIST = new Action<>(Action.VIEW_LIST);
    public static Action<JsonSchemaDocument> CREATE = new Action<>(Action.CREATE);
    public static Action<JsonSchemaDocument> MODIFY = new Action<>(Action.MODIFY);
    public static Action<JsonSchemaDocument> DELETE = new Action<>(Action.DELETE);
    public static Action<JsonSchemaDocument> CLAIM = new Action<>(Action.CLAIM);
    public static Action<JsonSchemaDocument> ASSIGN = new Action<>(Action.ASSIGN);
    public static Action<JsonSchemaDocument> ASSIGNABLE = new Action<>(Action.ASSIGNABLE);

    @NotNull
    @Override
    public List<Action<JsonSchemaDocument>> getAvailableActions() {
        return List.of(VIEW, VIEW_LIST, CREATE, MODIFY, DELETE, CLAIM, ASSIGN, ASSIGNABLE);
    }
}
