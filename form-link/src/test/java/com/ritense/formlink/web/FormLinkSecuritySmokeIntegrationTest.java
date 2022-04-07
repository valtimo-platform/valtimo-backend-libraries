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

package com.ritense.formlink.web;

import com.ritense.valtimo.web.rest.CoreSecuritySmokeIntegrationTest;
import java.util.Set;

public class FormLinkSecuritySmokeIntegrationTest extends CoreSecuritySmokeIntegrationTest {

    @Override
    protected Set<String> getIgnoredPathPatterns() {
        final Set<String> ignoredPathPatterns = super.getIgnoredPathPatterns();
        ignoredPathPatterns.add("/api/form-file");
        return ignoredPathPatterns;
    }
}