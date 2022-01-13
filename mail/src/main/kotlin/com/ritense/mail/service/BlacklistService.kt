package com.ritense.mail.service

import com.ritense.mail.domain.blacklist.BlacklistedEmail
import com.ritense.mail.repository.BlacklistRepository
import com.ritense.valtimo.contract.basictype.EmailAddress
import java.time.LocalDateTime

class BlacklistService(
    private val blacklistRepository: BlacklistRepository
) {
    @Deprecated("This method will be removed from 10.x",
        ReplaceWith("blacklist(emailAddress.toString(), LocalDateTime.now(), cause)"))
    fun blacklist(emailAddress: EmailAddress, cause: String) {
        blacklist(emailAddress.toString(), LocalDateTime.now(), cause)
    }

    fun blacklist(emailAddress: String, blacklistedOn: LocalDateTime, cause: String) {
        if (!isBlacklisted(emailAddress)) {
            blacklistRepository.save(BlacklistedEmail(emailAddress, blacklistedOn, cause))
        }
    }

    fun isBlacklisted(emailAddress: String): Boolean {
        return blacklistRepository.existsById(emailAddress)
    }

}