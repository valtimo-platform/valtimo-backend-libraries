/*
 * Copyright 2015-2025 Ritense BV, the Netherlands.
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

package com.ritense.widget

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.valtimo.contract.config.LiquibaseMasterChangeLogLocation
import com.ritense.valueresolver.IkoValueResolverService
import com.ritense.valueresolver.ValueResolverService
import com.ritense.widget.collection.CollectionWidget
import com.ritense.widget.collection.CollectionWidgetDataProvider
import com.ritense.widget.custom.CustomWidget
import com.ritense.widget.domain.Widget
import com.ritense.widget.fields.FieldsWidget
import com.ritense.widget.fields.FieldsWidgetDataProvider
import com.ritense.widget.repository.WidgetRepository
import com.ritense.widget.service.WidgetService
import com.ritense.widget.table.TableWidget
import com.ritense.widget.table.TableWidgetDataProvider
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.core.Ordered.HIGHEST_PRECEDENCE
import org.springframework.core.annotation.Order
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import javax.sql.DataSource

@AutoConfiguration
@EnableJpaRepositories(
    basePackageClasses = [
        WidgetRepository::class,
    ]
)
@EntityScan(
    basePackageClasses = [
        CollectionWidget::class,
        CustomWidget::class,
        FieldsWidget::class,
        TableWidget::class,
        Widget::class,
    ]
)
class WidgetAutoConfiguration {

    @Order(HIGHEST_PRECEDENCE + 34)
    @Bean
    @ConditionalOnClass(DataSource::class)
    @ConditionalOnMissingBean(name = ["widgetLiquibaseMasterChangeLogLocation"])
    fun widgetLiquibaseMasterChangeLogLocation(): LiquibaseMasterChangeLogLocation {
        return LiquibaseMasterChangeLogLocation("config/liquibase/widget-master.xml")
    }

    @Bean
    @ConditionalOnClass(DataSource::class)
    fun widgetService(
        widgetRepository: WidgetRepository,
        widgetDataProviders: List<WidgetDataProvider<*>>,
        valueResolverService: ValueResolverService,
    ): WidgetService {
        return WidgetService(
            widgetRepository,
            widgetDataProviders as List<WidgetDataProvider<Widget>>,
            valueResolverService,
        )
    }

    @ConditionalOnMissingBean(WidgetAnnotatedClassResolver::class)
    @Bean
    fun widgetAnnotatedClassResolver(
        context: ApplicationContext
    ) = WidgetAnnotatedClassResolver(context)

    @ConditionalOnMissingBean(WidgetJacksonModule::class)
    @Bean
    fun widgetJacksonModule(
        annotatedClassResolver: WidgetAnnotatedClassResolver
    ) = WidgetJacksonModule(annotatedClassResolver)

    @ConditionalOnMissingBean(FieldsWidgetDataProvider::class)
    @Bean
    fun fieldsWidgetDataProvider(
        valueResolverService: IkoValueResolverService,
    ) = FieldsWidgetDataProvider(valueResolverService)

    @ConditionalOnMissingBean(TableWidgetDataProvider::class)
    @Bean
    fun tableWidgetDataProvider(
        objectMapper: ObjectMapper,
        valueResolverService: IkoValueResolverService,
    ) = TableWidgetDataProvider(objectMapper, valueResolverService)

    @ConditionalOnMissingBean(CollectionWidgetDataProvider::class)
    @Bean
    fun collectionWidgetDataProvider(
        objectMapper: ObjectMapper,
        valueResolverService: IkoValueResolverService,
    ) = CollectionWidgetDataProvider(objectMapper, valueResolverService)

}