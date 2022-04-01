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

package com.ritense.valtimo.helper;

import org.camunda.bpm.engine.impl.QueryOrderingProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CamundaOrderByHelperTest {

    private Sort validSortAscendingForHistoricProcessInstance;
    private Sort validSortDescendingForHistoricProcessInstanceWithTwoProperties;
    private Sort invalidSortAscendingForHistoricProcessInstance;
    private Sort invalidSortDescendingForHistoricProcessInstance;

    @BeforeEach
    void setUp() {
        validSortAscendingForHistoricProcessInstance = Sort.by(Sort.Direction.ASC, "START_TIME_");
        validSortDescendingForHistoricProcessInstanceWithTwoProperties = Sort.by(Sort.Direction.DESC, "START_TIME_", "END_TIME_");
        invalidSortAscendingForHistoricProcessInstance = Sort.by(Sort.Direction.ASC, "contains_invalid_characters <>,.");
        invalidSortDescendingForHistoricProcessInstance = Sort.by(Sort.Direction.DESC, "valid_characters_but_name_is_not_recognized");
    }

    @Test
    void testSortToOrdersHappyPathWithOneProperty() {

        List<QueryOrderingProperty> queryOrderingProperties = CamundaOrderByHelper
            .sortToOrders("HistoricProcessInstance", validSortAscendingForHistoricProcessInstance);

        assertEquals(1, queryOrderingProperties.size());
        assertNotNull(queryOrderingProperties.get(0).getQueryProperty().getName());
        assertEquals("asc", queryOrderingProperties.get(0).getDirection().getName());
    }

    @Test
    void testSortToOrdersHappyPathWithMultipleProperties() {

        List<QueryOrderingProperty> queryOrderingProperties = CamundaOrderByHelper
            .sortToOrders("HistoricProcessInstance", validSortDescendingForHistoricProcessInstanceWithTwoProperties);

        assertTrue(queryOrderingProperties.size() > 1);
        assertNotNull(queryOrderingProperties.get(0).getQueryProperty().getName());
        assertNotNull(queryOrderingProperties.get(1).getQueryProperty().getName());
        assertEquals("desc", queryOrderingProperties.get(0).getDirection().getName());
        assertEquals("desc", queryOrderingProperties.get(1).getDirection().getName());
    }

    @Test
    void testSortToOrdersWithInvalidCamundaEntityName() {
        assertThrows(IllegalArgumentException.class, () -> {
            CamundaOrderByHelper
                .sortToOrders("not a valid value", validSortAscendingForHistoricProcessInstance);
        });
    }

    @Test
    void testSortToOrdersWithSortPropertyContainingDisallowedCharacters() {
        assertThrows(IllegalArgumentException.class, () -> {
            CamundaOrderByHelper.sortToOrders("HistoricProcessInstance", invalidSortAscendingForHistoricProcessInstance);
        });
    }

    @Test
    void testSortToOrdersWithUnrecognizedSortProperty() {
        assertThrows(IllegalArgumentException.class, () -> {
            CamundaOrderByHelper.sortToOrders("HistoricProcessInstance", invalidSortDescendingForHistoricProcessInstance);
        });
    }

    @Test
    void testOrderByHappyPathWithContainedProperty() {
        List<QueryOrderingProperty> queryOrderingProperties = CamundaOrderByHelper
            .sortToOrders("HistoricProcessInstance", validSortAscendingForHistoricProcessInstance);
        String orderBy = CamundaOrderByHelper.orderBy(queryOrderingProperties.get(0), "tableAlias");

        assertEquals("tableAlias.START_TIME_ asc", orderBy);
    }

    @Test
    void testOrderByHappyPathWithNoncontainedProperty() {

        List<QueryOrderingProperty> queryOrderingProperties = CamundaOrderByHelper
            .sortToOrders("HistoricProcessInstance", validSortAscendingForHistoricProcessInstance);
        queryOrderingProperties.get(0).setRelation("a_relation");
        String orderBy = CamundaOrderByHelper.orderBy(queryOrderingProperties.get(0), "tableAlias");

        assertEquals("tableAlias_START_TIME_ asc", orderBy);
    }

}
