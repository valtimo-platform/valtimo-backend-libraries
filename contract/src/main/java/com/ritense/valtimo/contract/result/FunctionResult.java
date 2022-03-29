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

package com.ritense.valtimo.contract.result;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotNull;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertStateTrue;

/**
 * Object representing the result of a function. The result can either be the expected output data (resultingValue)
 * or one or more errors.
 *
 * @param <T> Type of the resulting value
 * @param <E> Type of the OperationError, for fine-graned control over the error information transferred
 */
public interface FunctionResult<T, E extends OperationError> {

    Optional<T> resultingValue();

    List<E> errors();

    /**
     * Indicates if this FunctionResult has a resulting value.
     *
     * @return True if value present, false if errors.
     */
    default boolean hasResult() {
        // Default implementation provided for faster/easier ad-hoc subclassing
        return resultingValue().isPresent();
    }

    /**
     * Indicates if this FunctionResult did not succeed, and returned errors instead.
     *
     * @return True if function did not lead to expected result
     */
    default boolean isError() {
        return !hasResult();
    }

    /////////////////////////////////
    //// DEFAULT IMPLEMENTATIONS ////
    /////////////////////////////////

    /**
     * A simple, default implementation of a 'successful' FunctionResult. That is, one that resulted in a value
     *
     * @param <T> The type of the result
     */
    class Successful<T> implements FunctionResult<T, OperationError> {

        private final T resultingValue;

        public Successful(T resultingValue) {
            assertArgumentNotNull(resultingValue, "resultingValue is required");
            this.resultingValue = resultingValue;
        }

        @Override
        public Optional<T> resultingValue() {
            return Optional.of(resultingValue);
        }

        @Override
        public List<OperationError> errors() {
            return Collections.emptyList();
        }

        @Override
        public boolean hasResult() {
            return true;
        }
    }

    /**
     * A simple, default implementation of an 'error' FunctionResult. That is, one that did not result in expected result
     * but generated one or more errors instead.
     *
     * @param <T> The type of the result
     */
    class Erroneous<T> implements FunctionResult<T, OperationError> {

        private final List<OperationError> errors;

        public Erroneous(OperationError error) {
            this(List.of(error));
        }

        Erroneous(List<OperationError> errors) {
            assertArgumentNotNull(errors, "errors may not be null");
            assertStateTrue(errors.size() > 0, "errors may not be empty");
            this.errors = errors;
        }

        @Override
        public Optional<T> resultingValue() {
            return Optional.empty();
        }

        @Override
        public List<OperationError> errors() {
            return errors;
        }

        @Override
        public boolean hasResult() {
            return false;
        }
    }

}
