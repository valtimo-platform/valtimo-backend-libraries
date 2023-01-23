package com.ritense.objectmanagement.domain

import com.fasterxml.jackson.annotation.JsonCreator

class ObjectManagementConfigurationAutoDeploymentFinishedEvent {

    private var objectManagementConfigurations: List<ObjectManagement>? = null

    @JsonCreator
    fun objectManagementAutoDeploymentFinishedEvent(objectManagementConfigurations: ArrayList<ObjectManagement>?) {
        this.objectManagementConfigurations = objectManagementConfigurations
    }

    fun getObjectManagementConfigurations(): List<ObjectManagement>? {
        return objectManagementConfigurations
    }

}
