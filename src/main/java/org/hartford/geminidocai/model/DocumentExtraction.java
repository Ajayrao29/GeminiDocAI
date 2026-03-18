package org.hartford.GeminiDocAI.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentExtraction {
    private String type;
    private String name;
    private String policyNumber;
    private String date;
    private Double confidenceScore; // bonus from Phase 4
    private Map<String, Object> allExtractedFields; // for multi-doc support
}
