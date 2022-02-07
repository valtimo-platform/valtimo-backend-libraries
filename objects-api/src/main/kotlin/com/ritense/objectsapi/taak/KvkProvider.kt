package com.ritense.objectsapi.taak

import org.camunda.bpm.engine.delegate.DelegateExecution

interface KvkProvider {
    fun getKvkNummer(delegateExecution: DelegateExecution): String?
}
