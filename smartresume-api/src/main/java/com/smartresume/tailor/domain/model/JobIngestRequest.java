package com.smartresume.tailor.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobIngestRequest {
    @Builder.Default
    private List<String> keywords = new ArrayList<>();
    @Builder.Default
    private List<String> locations = new ArrayList<>();
    @Builder.Default
    private int limit = 10;
    private String source; // e.g., RAPIDAPI, MOCK, SCRAPER
}
