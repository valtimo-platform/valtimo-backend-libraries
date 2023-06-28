package com.ritense.valtimo.poc

class RecordRepositoryFactory(
    val recordRepositories: List<RecordRepository>
) {

    fun getRepository(caseRecord: CaseRecord): RecordRepository {
        return recordRepositories.single { it.supports(caseRecord.recordType) }
    }

}