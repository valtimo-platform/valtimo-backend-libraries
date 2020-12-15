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

package com.ritense.valtimo.contract.hardening.throwable;

public class UnsanitizedThrowable extends Throwable {
    private final String reasonNotSanitized;

    private UnsanitizedThrowable(Throwable originalException, String reasonNotSanitized) {
        super(originalException.getMessage(), originalException.getCause());
        this.reasonNotSanitized = reasonNotSanitized;
    }

    public String getReasonNotSanitized() {
        return reasonNotSanitized;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + ": " + getMessage();
    }

    public static UnsanitizedThrowable withReason(Throwable originalException, String reasonNotSanitized) {
        return new UnsanitizedThrowable(originalException, reasonNotSanitized);
    }
}
