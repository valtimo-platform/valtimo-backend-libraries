package com.ritense.valtimo.web.rest.dto;

import javax.validation.constraints.NotNull;

public class ChoiceFieldUpdateRequestDTO {
    @NotNull
    private Long id;
    @NotNull
    private String keyName;
    @NotNull
    private String title;

    public ChoiceFieldUpdateRequestDTO() {
    }

    public Long getId() {
        return id;
    }

    public String getKeyName() {
        return keyName;
    }

    public String getTitle() {
        return title;
    }
}
