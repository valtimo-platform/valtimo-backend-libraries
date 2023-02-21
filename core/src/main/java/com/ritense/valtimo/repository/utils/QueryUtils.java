/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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

package com.ritense.valtimo.repository.utils;

import org.springframework.data.domain.Sort;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.Collectors;

public class QueryUtils {

    public static String toOrders(Sort sort) {
        ArrayList<String> sqlOrders = new ArrayList<>();
        Iterator<Sort.Order> sortOrder = sort.iterator();
        while (sortOrder.hasNext()) {
            Sort.Order order = sortOrder.next();
            String property = order.getProperty();
            if (property != null && property.matches("^[0-9a-zA-Z$_]+$")) {
                sqlOrders.add(property + " " + order.getDirection().name());
            }
        }
        String sqlOrderStatement = sqlOrders.stream().collect(Collectors.joining(","));
        return sqlOrderStatement;
    }

}
