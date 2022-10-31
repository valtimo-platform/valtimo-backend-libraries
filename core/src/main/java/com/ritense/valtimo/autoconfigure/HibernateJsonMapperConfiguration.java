/*
 * Copyright 2015-2020 Ritense BV, the Netherlands.
 *
 * Licensed under EUPL, Version 1.2 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ritense.valtimo.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ritense.valtimo.config.HibernateObjectMapperSupplier;
import java.util.function.Supplier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HibernateJsonMapperConfiguration {

    /*
     * Sets up hibernate to use the HibernateObjectMapperSupplier to get the ObjectMapper
     */
    static {
        System.setProperty("hibernate.types.jackson.object.mapper", HibernateObjectMapperSupplier.class.getName());
    }

    /*
     * Makes sure that the hibernateObjectMapperSupplier is initialized, and the ObjectMapper is set before hibernate is
     * initialized.
     */
    @Bean
    public BeanFactoryPostProcessor hibernateDependencyProcessor() {
        return factory -> {
            BeanDefinition entityManagerDefinition = factory.getBeanDefinition("entityManagerFactory");

            String[] entityManagerDependencies = entityManagerDefinition.getDependsOn();
            entityManagerDependencies = entityManagerDependencies == null ? new String[]{} : entityManagerDependencies;

            String[] newDependencies = new String[entityManagerDependencies.length + 1];
            System.arraycopy(entityManagerDependencies, 0, newDependencies, 1, entityManagerDependencies.length);
            newDependencies[0] = "hibernateObjectMapperSupplier";

            entityManagerDefinition.setDependsOn(newDependencies);
        };
    }

    @Bean
    public Supplier<ObjectMapper> hibernateObjectMapperSupplier() {
        return new HibernateObjectMapperSupplier();
    }
}
