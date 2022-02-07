package com.ritense.objectsapi.taak

import org.camunda.bpm.engine.delegate.DelegateExecution

interface BsnProvider {
    fun getBurgerServiceNummer(delegateExecution: DelegateExecution): String?
}
