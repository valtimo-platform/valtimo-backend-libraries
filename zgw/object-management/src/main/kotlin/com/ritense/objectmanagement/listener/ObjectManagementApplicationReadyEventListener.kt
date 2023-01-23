package com.ritense.objectmanagement.listener

import com.ritense.objectmanagement.autodeployment.ObjectManagementDefinitionDeploymentService
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class ObjectManagementApplicationReadyEventListener(val objectManagementDefinitionDeploymentService: ObjectManagementDefinitionDeploymentService?) {

    @EventListener(ApplicationReadyEvent::class)
    fun handleApplicationReady() {
        objectManagementDefinitionDeploymentService!!.deployAllFromResourceFiles()
    }

}
