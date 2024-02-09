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

package com.ritense.valtimo.exception;

public class ProcessNotDeployableException extends Exception {

    /**
     * @param fileName The name of the bpmn file that was to be deployed
     */

    private static final String BASE_MESSAGE = "Process with file name '%s' is not eligible to be deployed.";

    public ProcessNotDeployableException(String fileName) {
        super(String.format(BASE_MESSAGE, fileName));
    }
}
