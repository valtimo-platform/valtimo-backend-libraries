package com.ritense.objectsapi.taak

import org.camunda.bpm.engine.delegate.DelegateTask

interface BsnProvider {
    fun getBurgerServiceNummer(task:DelegateTask): String?
}
