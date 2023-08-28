/*
 *  Copyright 2015-2023 Ritense BV, the Netherlands.
 *
 *  Licensed under EUPL, Version 1.2 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.ritense.formflow.expression

import com.ritense.formflow.BaseIntegrationTest
import com.ritense.formflow.expression.spel.SpelExpressionProcessorFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext

class ExpressionProcessorFactoryHolderIntTest: BaseIntegrationTest() {
    @Autowired
    lateinit var applicationContext: ApplicationContext

    @Test
    fun `can find form flow beans by annotation`() {
        ExpressionProcessorFactoryHolder
            .setInstance(SpelExpressionProcessorFactory(), applicationContext = applicationContext)

        val spelExpressionProcessorFactory =
            ExpressionProcessorFactoryHolder.getInstance() as SpelExpressionProcessorFactory

        assertNotNull(spelExpressionProcessorFactory.formFlowBeans)
        assertEquals(1, spelExpressionProcessorFactory.formFlowBeans.size)
        assertTrue(spelExpressionProcessorFactory.formFlowBeans["formFlowBeanTestHelper"] is FormFlowBeanTestHelper)
    }

    @Test
    fun `should use registered bean to execute expression`() {
        ExpressionProcessorFactoryHolder
            .setInstance(SpelExpressionProcessorFactory(), applicationContext = applicationContext)
        val evaluationResult = ExpressionProcessorFactoryHolder
            .getInstance()
            .create()
            .process<Any>("\${formFlowBeanTestHelper.returnTrue()}")

        assertTrue(evaluationResult as Boolean)

    }

    @Test
    fun `should fail to use registered bean to execute expression`() {
        ExpressionProcessorFactoryHolder
            .setInstance(SpelExpressionProcessorFactory(), applicationContext = applicationContext)
        val expressionProcessor = ExpressionProcessorFactoryHolder
            .getInstance()
            .create()

        assertThrows<ExpressionExecutionException> {
            expressionProcessor
                .process<Any>("\${formFlowService.findLatestDefinitionByKey(\"inkomens_loket\")}")
        }
    }

}
