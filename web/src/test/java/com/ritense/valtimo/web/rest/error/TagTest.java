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

package com.ritense.valtimo.web.rest.error;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class TagTest {

    @Test
    public void fakeNonTaggedTest() {
        assertEquals(21, Integer.sum(21, 0));
    }

    @Tag("unit")
    @Test
    public void fakeUnitTest() {
        assertEquals(21, Integer.sum(21, 0));
    }

    @Tag("security")
    @Test
    public void fakeSecurityTest() {
        assertEquals(21, Integer.sum(21, 0));
    }

    @Tag("integration")
    @Test
    public void fakeIntegrationTest() {
        assertEquals(2147483646, Integer.sum(2147183646, 300000));
    }

}
