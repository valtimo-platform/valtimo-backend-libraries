package com.ritense.valtimo.poc

import java.util.UUID

class TestPoc {

    fun foo() {
        val myCase = Case(UUID.randomUUID(), setOf())
        val recordRepositoryFactory: RecordRepositoryFactory = RecordRepositoryFactory(listOf())
        val caseRecord = myCase.caseRecords.first()
        val record = recordRepositoryFactory.getRepository(caseRecord).findByRecord<Record>(caseRecord)

        formFillerService.fillFormat(record, form)
    }



}


class FormFillerService(
    val formFillers: List<FormFillerInterface>
)

class