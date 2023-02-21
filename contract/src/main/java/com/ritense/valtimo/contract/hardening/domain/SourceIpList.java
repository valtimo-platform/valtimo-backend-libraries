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

package com.ritense.valtimo.contract.hardening.domain;

import java.util.Set;

public class SourceIpList {
    private final Set<String> whitelist;
    private final Set<String> ips;

    public SourceIpList(Set<String> whitelist, Set<String> ips) {
        this.whitelist = whitelist;
        this.ips = ips;
    }

    public boolean isWhitelisted() {
        if (whitelist.contains("*")) {
            return true;
        }
        return ips.stream().anyMatch(whitelist::contains);
    }

    public String getWhiteListedIp() {
        if (this.whitelist.contains("*")) {
            return "*";
        } else {
            return ips.stream().filter(whitelist::contains).findFirst().orElse(null);
        }
    }
}