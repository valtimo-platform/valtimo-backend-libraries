package com.ritense.objectmanagement.service

import com.ritense.objectmanagement.domain.ObjectManagement
import com.ritense.objectmanagement.repository.ObjectManagementRepository
import java.util.UUID
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class ObjectManagementService(
    private val objectManagementRepository: ObjectManagementRepository
) {

    fun createAndUpdate(objectManagement: ObjectManagement): ObjectManagement {
        val databaseObjectManagement = objectManagementRepository.findByTitle(objectManagement.title)
        if (databaseObjectManagement != null){
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "This title already exists please chose another title"
                )
        }
        return objectManagementRepository.save(objectManagement)
    }

    fun getById(id: UUID): ObjectManagement? {
        return objectManagementRepository.findByIdOrNull(id)
    }

    fun getAll(): MutableList<ObjectManagement> {
        return objectManagementRepository.findAll()
    }
}