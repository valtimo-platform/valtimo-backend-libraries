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

package com.ritense.document.repository.impl;

import com.ritense.document.domain.impl.JsonSchemaDocument;
import com.vladmihalcea.hibernate.type.AbstractHibernateType;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import com.vladmihalcea.hibernate.type.json.JsonStringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

public abstract class AbstractDbUtil {
    private static final Logger logger = LoggerFactory.getLogger(AbstractDbUtil.class);

    protected ApplicationEventPublisher applicationEventPublisher;
    protected String dbType;

    protected AbstractHibernateType getJsonType() {
        if (dbType.equals("mysql")) {
            return JsonStringType.INSTANCE;
        } else if (dbType.equals("postgres")) {
            return JsonBinaryType.INSTANCE;
        } else {
            throw new UnsupportedOperationException("Unknown DB type (" + dbType + ") ");
        }
    }

    protected void publishEvents(JsonSchemaDocument document) {
        logger.debug(
            "onFlushEntity: Processing aggregate root " +
                "events (count=${entity.domainEvents().size}) "
        );
        document.domainEvents().forEach(
            domainEvent -> {
                applicationEventPublisher.publishEvent(domainEvent);
            }
        );
        document.clearDomainEvents();
    }
}