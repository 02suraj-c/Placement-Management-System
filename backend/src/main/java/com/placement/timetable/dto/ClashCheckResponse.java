package com.placement.timetable.dto;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClashCheckResponse {
    private boolean hasClash;
    private List<String> clashDetails;
}

