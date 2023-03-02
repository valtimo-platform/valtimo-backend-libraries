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

package com.ritense.documentgeneration.service.exception;

public class GeneratorNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 886081700837656494L;

    public GeneratorNotFoundException(String message) {
        super(message);
    }

    public GeneratorNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public GeneratorNotFoundException(Throwable cause) {
        super(cause);
    }

    public GeneratorNotFoundException(
        String message,
        Throwable cause,
        boolean enableSuppression,
        boolean writableStackTrace
    ) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}