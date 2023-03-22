package com.ritense.document.service;

public class PBACFilter {
    public PBACFilter(String field, String value, String operation) {
        this.field = field;
        this.value = value;
        this.operation = operation;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    String field;
    String value;
    String operation;
}
