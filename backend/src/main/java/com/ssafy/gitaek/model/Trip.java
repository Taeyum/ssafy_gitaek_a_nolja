package com.ssafy.gitaek.model;

import lombok.Data;
import java.time.LocalDate;

@Data
public class Trip {
    private int tripId;
    private String title;
    private String style;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private int ownerId;
    private int maxParticipants;
    private int regionId;
    private int currentParticipants;
    private Integer currentEditorId;
}