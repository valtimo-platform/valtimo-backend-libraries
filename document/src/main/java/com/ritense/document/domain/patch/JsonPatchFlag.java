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

package com.ritense.document.domain.patch;

import static com.flipkart.zjsonpatch.CompatibilityFlags.ALLOW_MISSING_TARGET_OBJECT_ON_REPLACE;
import static com.flipkart.zjsonpatch.CompatibilityFlags.MISSING_VALUES_AS_NULLS;
import static com.flipkart.zjsonpatch.CompatibilityFlags.REMOVE_NONE_EXISTING_ARRAY_ELEMENT;

import com.flipkart.zjsonpatch.CompatibilityFlags;
import java.util.EnumSet;

class JsonPatchFlag {

    private JsonPatchFlag() {
    }

    static EnumSet<CompatibilityFlags> defaultCompatibilityFlags() {
        return EnumSet.of(
            MISSING_VALUES_AS_NULLS,
            REMOVE_NONE_EXISTING_ARRAY_ELEMENT,
            ALLOW_MISSING_TARGET_OBJECT_ON_REPLACE
        );
    }

}