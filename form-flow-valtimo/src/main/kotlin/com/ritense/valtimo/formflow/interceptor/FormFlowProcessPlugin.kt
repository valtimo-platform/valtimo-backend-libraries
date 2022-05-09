package com.ritense.valtimo.formflow.interceptor

import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl

class FormFlowProcessPlugin(
    val formFlowCreateTaskCommandInterceptor: FormFlowCreateTaskCommandInterceptor
) : AbstractProcessEnginePlugin() {

    override fun preInit(processEngineConfiguration: ProcessEngineConfigurationImpl) {
        var customPreCommandInterceptorsTxRequired = processEngineConfiguration.customPreCommandInterceptorsTxRequired
        if (customPreCommandInterceptorsTxRequired == null) {
            customPreCommandInterceptorsTxRequired = mutableListOf()
        }
        customPreCommandInterceptorsTxRequired.add(0, formFlowCreateTaskCommandInterceptor)
        processEngineConfiguration.customPreCommandInterceptorsTxRequired = customPreCommandInterceptorsTxRequired
    }
}