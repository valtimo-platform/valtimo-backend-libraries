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

package com.ritense.document.domain.patch;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.regex.Pattern;

public interface JsonPatchFilter {

    static void filter(JsonNode patch, EnumSet<JsonPatchFilterFlag> flags) {
        Iterator<JsonNode> item = patch.iterator();
        while (item.hasNext()) {
            JsonNode operation = item.next();
            if (flags.contains(JsonPatchFilterFlag.SKIP_REMOVAL_OPERATIONS)) {
                if (operation.get("op").asText().equals("remove")) {
                    item.remove();
                }
            } else if (flags.contains(JsonPatchFilterFlag.ALLOW_ARRAY_REMOVAL_ONLY)) {
                if (operation.get("op").asText().equals("remove")
                    && !arrayPattern().matcher(operation.get("path").asText()).matches()
                ) {
                    item.remove();
                }
            }
        }
    }

    static Pattern arrayPattern() {
        return Pattern.compile(".*/[0-9]");
    }

}
