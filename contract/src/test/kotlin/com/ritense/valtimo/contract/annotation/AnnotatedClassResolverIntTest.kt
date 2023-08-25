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

package com.ritense.valtimo.contract.annotation

import com.notwhitelisted.NonWhitelistedClassWithTextAnnotation
import com.ritense.valtimo.contract.BaseIntegrationTest
import com.ritense.valtimo.contract.custom.TestAnnotatedClassResolver
import com.ritense.valtimo.contract.custom.TestAnnotation
import com.whitelisted.WhitelistedClassWithTextAnnotation
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class AnnotatedClassResolverIntTest : BaseIntegrationTest() {

    @Autowired
    lateinit var testAnnotatedClassResolver: TestAnnotatedClassResolver

    @Test
    fun `should find annotated classes in whitelisted package`() {

        val annotatedClasses = testAnnotatedClassResolver.findClassesWithAnnotation<TestAnnotation>().keys

        assertTrue(annotatedClasses.contains(WhitelistedClassWithTextAnnotation::class.java))
        assertFalse(annotatedClasses.contains(NonWhitelistedClassWithTextAnnotation::class.java))
    }

    @Test
    fun `should find annotated methods in whitelisted package`() {

        val annotatedMethods = testAnnotatedClassResolver.findMethodsWithAnnotation<TestAnnotation>()

        assertTrue(annotatedMethods.any { it.name == "whitelistedAnnotatedMethod" })
        assertFalse(annotatedMethods.any { it.name == "whitelistedNonAnnotatedMethod" })
        assertFalse(annotatedMethods.any { it.name == "nonWhitelistedAnnotatedMethod" })
        assertFalse(annotatedMethods.any { it.name == "nonWhitelistedNonAnnotatedMethod" })
    }

}
