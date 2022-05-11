package com.ritense.valtimo.formflow.interceptor

import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl

class FormFlowProcessPlugin(
    val formFlowCreateTaskCommandInterceptor: FormFlowCreateTaskCommandInterceptor
) : AbstractProcessEnginePlugin() {

    override fun preInit(processEngineConfiguration: ProcessEngineConfigurationImpl) {
        var customPostCommandInterceptorsTxRequired = processEngineConfiguration.customPostCommandInterceptorsTxRequired
        if (customPostCommandInterceptorsTxRequired == null) {
            customPostCommandInterceptorsTxRequired = mutableListOf()
        }
        customPostCommandInterceptorsTxRequired.add(0, formFlowCreateTaskCommandInterceptor)
        processEngineConfiguration.customPostCommandInterceptorsTxRequired = customPostCommandInterceptorsTxRequired
    }
}