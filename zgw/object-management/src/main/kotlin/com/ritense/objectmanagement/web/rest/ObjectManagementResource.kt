package com.ritense.objectmanagement.web.rest

import com.ritense.objectmanagement.domain.ObjectManagement
import com.ritense.objectmanagement.service.ObjectManagementService
import java.util.UUID
import javax.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping


@RequestMapping("/api/v1/object/management/configuration")
class ObjectManagementResource(
    private val objectManagementService: ObjectManagementService
) {

    @PostMapping
    fun create(@Valid @RequestBody objectManagement: ObjectManagement): ObjectManagement {
        return objectManagementService.createAndUpdate(objectManagement)
    }

    @PutMapping
    fun update(@Valid @RequestBody objectManagement: ObjectManagement): ObjectManagement {
        return objectManagementService.createAndUpdate(objectManagement)
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: UUID): ObjectManagement? {
        return objectManagementService.getById(id)
    }

    @GetMapping
    fun getAll(): MutableList<ObjectManagement> {
        return objectManagementService.getAll()
    }

}