package com.ritense.openproduct.config

import com.ritense.openproduct.resolver.ProductValueResolverFactory
import com.ritense.openproduct.service.ProductService
import com.ritense.processdocument.service.ProcessDocumentService
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan

@AutoConfiguration
@EntityScan("com.ritense.openproduct")
@ComponentScan("com.ritense.openproduct")
class ProductResolverAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ProductValueResolverFactory::class)
    fun productValueResolverFactory(
        productService: ProductService,
        processDocumentService: ProcessDocumentService
    ) = ProductValueResolverFactory(
        productService,
        processDocumentService
    )
}
