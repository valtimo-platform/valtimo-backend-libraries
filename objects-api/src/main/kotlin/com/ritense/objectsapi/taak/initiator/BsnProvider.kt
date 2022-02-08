package com.ritense.objectsapi.taak.initiator

import org.camunda.bpm.engine.delegate.DelegateTask

interface BsnProvider {
    fun getBurgerServiceNummer(task:DelegateTask): String?
}
