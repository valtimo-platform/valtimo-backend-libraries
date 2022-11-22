package com.ritense.mail.service

import com.ritense.mail.domain.blacklist.BlacklistedEmail
import com.ritense.mail.repository.BlacklistRepository
import java.time.LocalDateTime

class BlacklistService(
    private val blacklistRepository: BlacklistRepository
) {

    fun blacklist(emailAddress: String, blacklistedOn: LocalDateTime, cause: String) {
        if (!isBlacklisted(emailAddress)) {
            blacklistRepository.save(BlacklistedEmail(emailAddress, blacklistedOn, cause))
        }
    }

    fun isBlacklisted(emailAddress: String): Boolean {
        return blacklistRepository.existsById(emailAddress)
    }

}