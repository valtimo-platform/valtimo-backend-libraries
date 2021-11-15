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

package com.ritense.valtimo.viewconfigurator.domain;

public class ViewConfigurationRequestVariable implements Comparable<ViewConfigurationRequestVariable> {

    private Long id;

    private int sequence;

    private ViewConfigurationRequestVariable() {
    }

    public ViewConfigurationRequestVariable(Long id, int sequence) {
        this.id = id;
        this.sequence = sequence;
    }

    public Long getId() {
        return id;
    }

    public int getSequence() {
        return sequence;
    }

    @Override
    public int compareTo(ViewConfigurationRequestVariable requestVariable) {
        final int before = -1;
        final int equal = 0;
        final int after = 1;

        if (this == requestVariable) {
            return equal;
        }

        if (this.getSequence() < requestVariable.getSequence()) {
            return before;
        }
        if (this.getSequence() > requestVariable.getSequence()) {
            return after;
        }

        return equal;
    }
}
