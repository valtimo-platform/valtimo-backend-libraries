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

package com.ritense.valtimo.repository;

import com.ritense.valtimo.repository.camunda.dto.TaskExtended;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.session.SqlSession;
import org.camunda.bpm.engine.impl.Direction;
import org.camunda.bpm.engine.impl.QueryOrderingProperty;
import org.camunda.bpm.engine.impl.TaskQueryProperty;
import org.camunda.bpm.engine.impl.db.ListQueryParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import static org.camunda.bpm.engine.impl.Direction.ASCENDING;
import static org.camunda.bpm.engine.impl.Direction.DESCENDING;

public class CamundaTaskRepository {

    private final SqlSession session;

    public CamundaTaskRepository(SqlSession session) {
        this.session = session;
    }

    public Page<TaskExtended> findTasks(Pageable pageable, Map<String, Object> parameters) {
        var query = new ListQueryParameterObject(
            parameters,
            pageable.getPageNumber() * pageable.getPageSize(),
            pageable.getPageSize()
        );
        if (pageable.getSort().isSorted()) {
            query.setOrderingProperties(getQueryOrderingProperties(pageable.getSort()));
        } else {
            query.setOrderingProperties(
                Collections.singletonList(new QueryOrderingProperty(TaskQueryProperty.CREATE_TIME, Direction.DESCENDING))
            );
        }
        List<TaskExtended> taskWithVariables = session.selectList(
            "com.ritense.valtimo.mapper.findTasks",
            query
        );
        Long taskCount = session.selectOne(
            "com.ritense.valtimo.mapper.findTasksCount",
            query
        );
        return new PageImpl<>(taskWithVariables, pageable, taskCount);
    }

    private List<QueryOrderingProperty> getQueryOrderingProperties(Sort sort) {
        return sort.map(this::getQueryOrderingProperty).toList();
    }

    private QueryOrderingProperty getQueryOrderingProperty(Sort.Order order) {
        final var direction = order.getDirection() == Sort.Direction.ASC ? ASCENDING : DESCENDING;
        switch (order.getProperty()) {
            case ("name"):
                return new QueryOrderingProperty(TaskQueryProperty.NAME, direction);
            case ("priority"):
                return new QueryOrderingProperty(TaskQueryProperty.PRIORITY, direction);
            case ("assignee"):
                return new QueryOrderingProperty(TaskQueryProperty.ASSIGNEE, direction);
            case ("created"):
                return new QueryOrderingProperty(TaskQueryProperty.CREATE_TIME, direction);
            case ("due"):
                return new QueryOrderingProperty(TaskQueryProperty.DUE_DATE, direction);
            case ("followUpDate"):
                return new QueryOrderingProperty(TaskQueryProperty.FOLLOW_UP_DATE, direction);
            default:
                throw new IllegalArgumentException("Unknown ordering property with name: '" + order.getProperty() + "'");
        }
    }
}
