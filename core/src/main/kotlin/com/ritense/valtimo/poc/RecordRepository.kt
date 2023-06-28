package com.ritense.valtimo.poc

interface RecordRepository {

    fun supports(recordType: String): Boolean

    fun <T: Record> findByRecord(caseRecord: CaseRecord): T
}