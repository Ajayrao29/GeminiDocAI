package org.hartford.GeminiDocAI.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobStatus {
    private String jobId;
    private String status; // PENDING, PROCESSING, COMPLETED, FAILED
    private LocalDateTime createdAt;
    private DocumentExtraction data;
    private Object errors;
}
