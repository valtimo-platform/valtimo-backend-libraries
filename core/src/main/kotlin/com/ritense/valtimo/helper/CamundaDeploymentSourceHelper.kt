package com.ritense.valtimo.helper


import com.ritense.valtimo.camunda.domain.CamundaDeploymentSource
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class CamundaDeploymentSourceHelper {
    private val store: MutableMap<String, CamundaDeploymentSource> = ConcurrentHashMap()

    fun store(source: CamundaDeploymentSource): String {
        val uuid = UUID.randomUUID().toString()
        store[uuid] = source
        return uuid
    }

    fun retrieve(uuid: String): CamundaDeploymentSource? {
        return store.remove(uuid)
    }
}