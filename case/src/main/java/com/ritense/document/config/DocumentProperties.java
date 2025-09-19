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

package com.ritense.document.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties(prefix = "valtimo.document")
public class DocumentProperties {

    private final boolean usePessimisticLocking;
    private final int lockTimeoutInMillis;

    @ConstructorBinding
    public DocumentProperties(Boolean usePessimisticLocking, Integer lockTimeoutInMillis) {
        this.usePessimisticLocking = usePessimisticLocking != null ? usePessimisticLocking : false;
        this.lockTimeoutInMillis = lockTimeoutInMillis != null ? lockTimeoutInMillis : 30000;
    }

    public boolean isUsePessimisticLocking() {
        return usePessimisticLocking;
    }

    public int getLockTimeoutInMillis() {
        return lockTimeoutInMillis;
    }

    public int getLockTimeoutInSeconds() {
        return lockTimeoutInMillis / 1000;
    }
}