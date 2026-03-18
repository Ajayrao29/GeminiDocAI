package org.hartford.GeminiDocAI.service;

import org.hartford.GeminiDocAI.model.DocumentExtraction;
import org.hartford.GeminiDocAI.model.JobStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;

@Service
public class DocumentTaskService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentTaskService.class);

    private final GeminiService geminiService;
    private final ValidationService validationService;
    private final FileStorageService storageService;

    public DocumentTaskService(GeminiService geminiService, ValidationService validationService, FileStorageService storageService) {
        this.geminiService = geminiService;
        this.validationService = validationService;
        this.storageService = storageService;
    }

    @Async
    public void processAsync(String jobId, JobStatus job) {
        logger.info("Starting background processing for job: {}", jobId);
        job.setStatus("PROCESSING");
        
        try {
            Path path = storageService.load(jobId);
            byte[] bytes = Files.readAllBytes(path);
            String base64 = Base64.getEncoder().encodeToString(bytes);
            String mimeType = Files.probeContentType(path);

            logger.info("Calling Gemini API for job: {}", jobId);
            DocumentExtraction extraction = geminiService.processDocument(base64, mimeType);
            
            logger.info("Validating extraction for job: {}", jobId);
            List<String> errors = validationService.validate(extraction);
            
            job.setData(extraction);
            if (!errors.isEmpty()) {
                logger.warn("Job {} completed with validation errors", jobId);
                job.setStatus("COMPLETED_WITH_ERRORS");
                job.setErrors(errors);
            } else {
                logger.info("Job {} completed successfully", jobId);
                job.setStatus("COMPLETED");
            }
            
        } catch (Exception e) {
            logger.error("Job {} failed: {}", jobId, e.getMessage());
            job.setStatus("FAILED");
            job.setErrors(e.getMessage());
        }
    }
}
