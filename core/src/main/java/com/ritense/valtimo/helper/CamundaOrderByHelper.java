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

import org.camunda.bpm.engine.impl.Direction;
import org.camunda.bpm.engine.impl.HistoricProcessInstanceQueryProperty;
import org.camunda.bpm.engine.impl.QueryOrderingProperty;
import org.camunda.bpm.engine.query.QueryProperty;
import org.springframework.data.domain.Sort;
import java.util.ArrayList;
import java.util.List;
import static org.apache.commons.lang3.exception.ExceptionUtils.rethrow;

public class CamundaOrderByHelper {

    public static String orderBy(QueryOrderingProperty orderingProperty, String tableAlias) {
        QueryProperty queryProperty = orderingProperty.getQueryProperty();
        StringBuilder sb = new StringBuilder();
        sb.append(tableAlias);
        if (orderingProperty.isContainedProperty()) {
            sb.append(".");
        } else {
            sb.append("_");
        }
        sb.append(queryProperty.getName());
        sb.append(" ");
        sb.append(orderingProperty.getDirection().getName());
        return sb.toString();
    }

    public static List<QueryOrderingProperty> sortToOrders(String camundaEntityName, Sort sort) {
        ArrayList<QueryOrderingProperty> queryOrderingProperties = new ArrayList<>();
        for (Sort.Order order : sort) {
            String property = order.getProperty();
            String direction = order.getDirection().toString();
            QueryOrderingProperty queryOrderingProperty;

            if (property.matches("^[0-9a-zA-Z$_]+$")) {
                try {
                    queryOrderingProperty = sortToOrder(camundaEntityName, property, direction);
                    queryOrderingProperties.add(queryOrderingProperty);

                } catch (IllegalArgumentException e) {
                    rethrow(e);
                }
            } else {
                throw (new IllegalArgumentException("Invalid query property name: " + property));
            }
        }
        return queryOrderingProperties;
    }

    private static QueryOrderingProperty sortToOrder(
        String camundaEntityName,
        String queryPropertyName,
        String directionName
    ) {
        QueryProperty queryProperty;
        if ("HistoricProcessInstance".equals(camundaEntityName)) {
            queryProperty = findQueryPropertyForHistoricProcessInstance(queryPropertyName);
            // This can be expanded with more Camunda entity types. For each of these, another private static function should be added to this Class.
        } else {
            throw (new IllegalArgumentException("Invalid Camunda entity reference: " + camundaEntityName));
        }
        Direction direction = findDirection(directionName);
        return new QueryOrderingProperty(queryProperty, direction);
    }

    private static Direction findDirection(String directionName) {
        Direction direction;
        switch (directionName.toUpperCase()) {
            case "ASC":
                direction = Direction.ASCENDING;
                break;
            case "DESC":
                direction = Direction.DESCENDING;
                break;
            default:
                direction = Direction.ASCENDING;
        }
        return direction;
    }

    private static QueryProperty findQueryPropertyForHistoricProcessInstance(String queryPropertyName) {
        QueryProperty queryProperty;
        switch (queryPropertyName.toUpperCase()) {
            case "PROC_INST_ID_":
                queryProperty = HistoricProcessInstanceQueryProperty.PROCESS_INSTANCE_ID_;
                break;
            case "PROC_DEF_ID_":
                queryProperty = HistoricProcessInstanceQueryProperty.PROCESS_DEFINITION_ID;
                break;
            case "PROC_DEF_KEY_":
                queryProperty = HistoricProcessInstanceQueryProperty.PROCESS_DEFINITION_KEY;
                break;
            case "NAME_":
                queryProperty = HistoricProcessInstanceQueryProperty.PROCESS_DEFINITION_NAME;
                break;
            case "VERSION_":
                queryProperty = HistoricProcessInstanceQueryProperty.PROCESS_DEFINITION_VERSION;
                break;
            case "BUSINESS_KEY_":
                queryProperty = HistoricProcessInstanceQueryProperty.BUSINESS_KEY;
                break;
            case "START_TIME_":
                queryProperty = HistoricProcessInstanceQueryProperty.START_TIME;
                break;
            case "END_TIME_":
                queryProperty = HistoricProcessInstanceQueryProperty.END_TIME;
                break;
            case "DURATION_":
                queryProperty = HistoricProcessInstanceQueryProperty.DURATION;
                break;
            case "TENANT_ID_":
                queryProperty = HistoricProcessInstanceQueryProperty.TENANT_ID;
                break;
            default:
                throw (new IllegalArgumentException("Invalid query property name: " + queryPropertyName));
        }
        return queryProperty;
    }

}