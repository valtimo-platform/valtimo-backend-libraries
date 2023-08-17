package com.ritense.valtimo.milestones.web.rest.dto;

public class MilestoneSetSaveDTO {
    private Long id;
    private String title;

    public MilestoneSetSaveDTO() {
        //Default constructor
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
