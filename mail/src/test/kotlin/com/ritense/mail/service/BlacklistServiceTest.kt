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