///*
// * Copyright 2015-2023 Ritense BV, the Netherlands.
// *
// * Licensed under EUPL, Version 1.2 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" basis,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package com.ritense.authorization
//
//import com.fasterxml.jackson.databind.ObjectMapper
//import org.mockito.internal.util.Supplier
//import org.springframework.beans.factory.config.BeanDefinition
//import org.springframework.beans.factory.config.BeanFactoryPostProcessor
//import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
//import org.springframework.context.annotation.Bean
//import org.springframework.context.annotation.Configuration
//
//@Configuration
//class HibernateJsonMapperConfiguration {
//    /*
//     * Makes sure that the hibernateObjectMapperSupplier is initialized, and the ObjectMapper is set before hibernate is
//     * initialized.
//     */
//    @Bean
//    fun hibernateDependencyProcessor(): BeanFactoryPostProcessor {
//        return BeanFactoryPostProcessor { factory: ConfigurableListableBeanFactory ->
//            val entityManagerDefinition: BeanDefinition = factory.getBeanDefinition("entityManagerFactory")
//            var entityManagerDependencies: Array<String?> = entityManagerDefinition.getDependsOn()
//            entityManagerDependencies = entityManagerDependencies ?: arrayOf()
//            val newDependencies = arrayOfNulls<String>(entityManagerDependencies.size + 1)
//            System.arraycopy(entityManagerDependencies, 0, newDependencies, 1, entityManagerDependencies.size)
//            newDependencies[0] = "hibernateObjectMapperSupplier"
//            entityManagerDefinition.setDependsOn(*newDependencies)
//        }
//    }
//
//    @Bean
//    fun hibernateObjectMapperSupplier(objectMapper: ObjectMapper): java.util.function.Supplier<ObjectMapper> {
//        return HibernateObjectMapperSupplier(objectMapper)
//    }
//
//    companion object {
//        /*
//     * Sets up hibernate to use the HibernateObjectMapperSupplier to get the ObjectMapper
//     */
//        init {
//            System.setProperty("hibernate.types.jackson.object.mapper", HibernateObjectMapperSupplier::class.java.name)
//        }
//    }
//}
