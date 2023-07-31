package com.ritense.valtimo.web.rest.dto;

import javax.validation.constraints.NotNull;

public class ChoiceFieldCreateRequestDTO {
    @NotNull
    private String keyName;
    @NotNull
    private String title;

    public ChoiceFieldCreateRequestDTO() {
    }

    public String getKeyName() {
        return keyName;
    }

    public String getTitle() {
        return title;
    }
}
