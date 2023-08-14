package com.ritense.document.repository.impl;

import com.vladmihalcea.hibernate.type.AbstractHibernateType;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import com.vladmihalcea.hibernate.type.json.JsonStringType;

public abstract class AbstractDbUtil {

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

}
