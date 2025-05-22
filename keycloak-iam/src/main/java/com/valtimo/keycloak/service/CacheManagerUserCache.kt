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

import com.ritense.valtimo.contract.annotation.SkipComponentScan
import mu.KotlinLogging
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.CacheEvict
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@SkipComponentScan
class CacheManagerUserCache(
    private val cacheManager: CacheManager,
) : UserCache {

    @Suppress("UNCHECKED_CAST")
    override fun <T> get(cacheType: CacheType, key: String, requestFunction: (String) -> T?): T? {
        val cache = cacheManager.getCache(cacheType.getCacheName())
            ?: return requestFunction(key)
        val wrapper = cache.get(key)
        if (wrapper != null) {
            logger.debug { "Returning user information from cache ${cacheType.getCacheName()} with key $key." }
            return wrapper.get() as T
        }

        logger.debug { "Cache miss for ${cacheType.getCacheName()} with key $key. Adding user to cache." }
        val newValue = requestFunction(key)
        if (newValue != null && !cacheType.cachedClass.java.isAssignableFrom(newValue!!::class.java)) {
            throw IllegalArgumentException("The type of the value returned by the request function (${newValue!!::class.java.name}) does not match the cache type (${cacheType.cachedClass.java.name})")
        }
        cache.put(key, newValue)
        return newValue
    }

    @CacheEvict(allEntries = true, value = ["EMAIL_ManageableUser", "USER_IDENTIFIER_ValtimoUser"])
    @Scheduled(fixedRateString = "\${valtimo.keycloak.cache.maxTtl:PT1H}")
    fun logCacheClear() {
        logger.debug { "Clearing all user information cache" }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
