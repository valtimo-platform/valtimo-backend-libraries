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

package com.ritense.valtimo.milestones.service.exception;

import com.ritense.valtimo.web.rest.error.EntityException;

public class MultipleProcessesWithinMilestoneSetException extends EntityException {
    public MultipleProcessesWithinMilestoneSetException(String message, String translationKey, String entityName) {
        super(message, translationKey, entityName);
    }

    public MultipleProcessesWithinMilestoneSetException(String message, Throwable cause, String translationKey, String entityName) {
        super(message, cause, translationKey, entityName);
    }
}
