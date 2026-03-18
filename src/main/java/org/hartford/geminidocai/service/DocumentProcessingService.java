package org.hartford.GeminiDocAI.service;

import org.hartford.GeminiDocAI.model.JobStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DocumentProcessingService {

    private final DocumentTaskService taskService;
    
    // In-memory status store
    private final Map<String, JobStatus> jobs = new ConcurrentHashMap<>();

    public DocumentProcessingService(DocumentTaskService taskService) {
        this.taskService = taskService;
    }

    public String submitJob(String fileId) {
        JobStatus job = JobStatus.builder()
                .jobId(fileId)
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();
        jobs.put(fileId, job);
        
        // Trigger async process via proxy-enabled bean
        taskService.processAsync(fileId, job);
        
        return fileId;
    }

    public JobStatus getStatus(String jobId) {
        return jobs.get(jobId);
    }
}
