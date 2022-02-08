package com.ritense.objectsapi.taak.initiator

import org.camunda.bpm.engine.delegate.DelegateTask

interface KvkProvider {
    fun getKvkNummer(task: DelegateTask): String?
}
