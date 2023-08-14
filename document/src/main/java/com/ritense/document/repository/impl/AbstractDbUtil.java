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