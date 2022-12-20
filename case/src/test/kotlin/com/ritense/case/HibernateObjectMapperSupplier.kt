package com.ritense.case

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import java.util.function.Supplier

class HibernateObjectMapperSupplier : Supplier<ObjectMapper> {

    @Autowired
    fun setObjectMapper(objectMapper: ObjectMapper) {
        Companion.objectMapper = objectMapper
    }

    override fun get(): ObjectMapper {
        return objectMapper!!
    }

    companion object {
        private var objectMapper: ObjectMapper? = null
    }
}
