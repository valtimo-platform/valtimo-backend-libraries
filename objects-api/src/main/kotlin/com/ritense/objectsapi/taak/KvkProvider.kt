package com.ritense.objectsapi.taak

import org.camunda.bpm.engine.delegate.DelegateTask

interface KvkProvider {
    fun getKvkNummer(task: DelegateTask): String?
}
