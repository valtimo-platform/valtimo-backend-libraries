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

package com.ritense.mail.service

import com.ritense.mail.BaseTest
import com.ritense.mail.domain.blacklist.BlacklistedEmail
import com.ritense.mail.repository.BlacklistRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import java.time.LocalDateTime

internal class BlacklistServiceTest : BaseTest() {

    lateinit var blacklistRepository: BlacklistRepository
    lateinit var blacklistService: BlacklistService
    val emailTest: String = "test@test.com"

    @BeforeEach
    fun setUp() {
        super.baseSetUp()
        blacklistRepository = mock(BlacklistRepository::class.java)
        blacklistService = spy(BlacklistService(blacklistRepository))
    }

    @Test
    fun shouldBlacklistEmailAddress() {
        `when`(blacklistRepository.existsById(emailTest)).thenReturn(false)
        val blacklistedOn = LocalDateTime.now()
        blacklistService.blacklist(emailTest, blacklistedOn, "test reason")

        val blacklistArgumentCaptor = ArgumentCaptor.forClass(BlacklistedEmail::class.java)
        verify(blacklistRepository).save(blacklistArgumentCaptor.capture())

        assertThat(blacklistArgumentCaptor.value.emailAddress).isEqualTo(emailTest)
        assertThat(blacklistArgumentCaptor.value.dateCreated).isEqualTo(blacklistedOn)
        assertThat(blacklistArgumentCaptor.value.cause).isEqualTo("test reason")
    }

    @Test
    fun shouldNotBlacklistEmailAddressForDuplicateEmailAddress() {
        `when`(blacklistRepository.existsById(emailTest)).thenReturn(true)
        blacklistService.blacklist(emailTest, LocalDateTime.now(), "test reason")
        verify(blacklistRepository, times(0)).save(any())
    }

    @Test
    fun isAlreadyBlacklistShouldReturnTrue() {
        `when`(blacklistRepository.existsById(emailTest)).thenReturn(true)
        blacklistService.blacklist(emailTest, LocalDateTime.now(), "test reason")

        val blacklisted = blacklistService.isBlacklisted(emailTest)
        assertThat(blacklisted).isTrue
    }

    @Test
    fun isNotBlacklistShouldReturnFalse() {
        `when`(blacklistRepository.existsById(emailTest)).thenReturn(false)
        blacklistService.blacklist(emailTest, LocalDateTime.now(), "test reason")

        val blacklisted = blacklistService.isBlacklisted(emailTest)
        assertThat(blacklisted).isFalse
    }

}