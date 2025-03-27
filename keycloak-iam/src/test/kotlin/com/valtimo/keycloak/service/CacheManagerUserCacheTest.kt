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

package com.valtimo.keycloak.service

import com.ritense.valtimo.contract.authentication.ManageableUser
import com.ritense.valtimo.contract.authentication.model.ValtimoUser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.cache.CacheManager
import org.springframework.cache.concurrent.ConcurrentMapCacheManager

class CacheManagerUserCacheTest {
    private lateinit var cacheManager: CacheManager
    private lateinit var cache: UserCache

    @BeforeEach
    fun setup() {
        cacheManager = ConcurrentMapCacheManager()
        cache = CacheManagerUserCache(cacheManager)
    }

    @Test
    fun `should return user information from cache`() {
        val cacheType = CacheType.EMAIL
        val key = "key"
        val expectedResult = mock<ManageableUser>()
        val requestFunction: (String) -> ManageableUser? = { expectedResult }

        val result = cache.get(cacheType, key, requestFunction)

        assertEquals(expectedResult, result)
    }

    @Test
    fun `should only call retrieval function once`() {
        // Arrange
        val cacheType = CacheType.EMAIL
        val key = "key"
        val expectedResult = mock<ManageableUser>()
        val mockRequestFunction = mock<UserRetrievalFunction>()
        val requestFunction: (String) -> ManageableUser? = {
            mockRequestFunction.retrieveUser(key)
            expectedResult
        }

        val result = cache.get(cacheType, key, requestFunction)
        val result2 = cache.get(cacheType, key, requestFunction)

        assertEquals(expectedResult, result)
        assertEquals(expectedResult, result2)
        verify(mockRequestFunction, times(1)).retrieveUser(key)
    }

    @Test
    fun `should use separate cache for different types`() {
        val cacheType1 = CacheType.EMAIL
        val cacheType2 = CacheType.USER_IDENTIFIER
        val key = "key"
        val mockRequestFunction = mock<UserRetrievalFunction>()
        // need 2 functions because of different return types, but we want to use the same mock to verify it gets called twice
        val expectedResult1 = mock<ManageableUser>()
        val requestFunction1: (String) -> ManageableUser? = {
            mockRequestFunction.retrieveUser(key)
            expectedResult1
        }
        val expectedResult2 = mock<ValtimoUser>()
        val requestFunction2: (String) -> ValtimoUser? = {
            mockRequestFunction.retrieveUser(key)
            expectedResult2
        }

        val result1 = cache.get(cacheType1, key, requestFunction1)
        val result2 = cache.get(cacheType2, key, requestFunction2)

        assertEquals(expectedResult1, result1)
        assertEquals(expectedResult2, result2)
        verify(mockRequestFunction, times(2)).retrieveUser(key)
    }

    @Test
    fun `should only cache expected types`() {
        val cacheType = CacheType.EMAIL
        val key = "key"
        val requestFunction: (String) -> String? = { key }

        val ex = assertThrows<IllegalArgumentException> {
            cache.get(cacheType, key, requestFunction)
        }

        assertEquals(
            "The type of the value returned by the request function (java.lang.String) does not match the cache type (com.ritense.valtimo.contract.authentication.ManageableUser)",
            ex.message
        )
    }

    interface UserRetrievalFunction {
        fun retrieveUser(key: String): Void
    }
}