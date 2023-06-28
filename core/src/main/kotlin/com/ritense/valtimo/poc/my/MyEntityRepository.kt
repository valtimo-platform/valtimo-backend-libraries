package com.ritense.valtimo.poc.my

import com.ritense.valtimo.poc.RecordRepository
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface MyEntityRepository: JpaRepository<MyEntity, UUID>, RecordRepository {

    override fun supports(recordType: String): Boolean {
        return MyEntity::class.simpleName == recordType
    }
}