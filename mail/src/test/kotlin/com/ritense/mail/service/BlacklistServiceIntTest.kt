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

package com.ritense.mail.service

import com.ritense.mail.BaseIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import javax.inject.Inject

@Transactional
internal class BlacklistServiceIntTest : BaseIntegrationTest() {

    @Inject
    lateinit var blacklistService: BlacklistService

    @Test
    fun `should backlist email`() {
        blacklistService.blacklist("test@test.com", LocalDateTime.now(), "RebelWithACause")
        assertThat(blacklistService.isBlacklisted("test@test.com")).isTrue
    }
}