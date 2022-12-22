package com.ritense.objectmanagement.repository

import com.ritense.objectmanagement.domain.ObjectManagement
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository

interface ObjectManagementRepository: JpaRepository<ObjectManagement, UUID> {

    fun findByTitle(title: String): ObjectManagement?
}