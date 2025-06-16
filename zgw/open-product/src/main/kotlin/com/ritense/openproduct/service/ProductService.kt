package com.ritense.openproduct.service

import org.springframework.stereotype.Service

@Service
class ProductService {

    fun doSomething(input: String ): String {
        return "Something - $input"
    }
}