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

package com.ritense.processdocument.service.impl.result;

import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotNull;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertStateTrue;

import com.ritense.document.domain.impl.JsonSchemaDocument;
import com.ritense.processdocument.service.result.ModifyDocumentAndCompleteTaskResult;
import com.ritense.processdocument.service.result.TransactionalResult;
import com.ritense.valtimo.contract.result.OperationError;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ModifyDocumentAndCompleteTaskResultFailed implements ModifyDocumentAndCompleteTaskResult, TransactionalResult {

    private final List<OperationError> errors;

    public ModifyDocumentAndCompleteTaskResultFailed(List<? extends OperationError> errors) {
        assertArgumentNotNull(errors, "errors may not be null");
        assertStateTrue(!errors.isEmpty(), "errors may not be empty");
        this.errors = new ArrayList<>(errors);
        rollback();
    }

    public ModifyDocumentAndCompleteTaskResultFailed(OperationError error) {
        assertArgumentNotNull(error, "error is required");
        this.errors = List.of(error);
        rollback();
    }

    @Override
    public Optional<JsonSchemaDocument> resultingDocument() {
        return Optional.empty();
    }

    @Override
    public List<OperationError> errors() {
        return errors;
    }

}