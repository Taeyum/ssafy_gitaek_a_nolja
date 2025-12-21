package com.ssafy.gitaek.dto;


import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ChecklistDto {
    private int checkId;
    private int userId;
    private int planId;
    private String content;
    
    @JsonProperty("isChecked") 
    private boolean isChecked;
}