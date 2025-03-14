package com.ritense.document.repository

import com.ritense.document.BaseIntegrationTest
import com.ritense.document.domain.CaseTag
import com.ritense.document.domain.CaseTagColor
import com.ritense.document.domain.CaseTagId
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional


@Transactional
class CaseTagRepositoryIntTest @Autowired constructor(
    private val repository: CaseTagRepository
) : BaseIntegrationTest() {

    @Test
    fun `should save a case tag`() {

        val tag = CaseTag(
            id = CaseTagId("bezwaar", "test"),
            title = "Some Tag",
            color = CaseTagColor.COOLGRAY,
            order = 1
        )

        repository.save(tag)
    }

}