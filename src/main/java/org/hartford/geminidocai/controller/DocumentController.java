package org.hartford.GeminiDocAI.controller;

import org.hartford.GeminiDocAI.service.DocumentProcessingService;
import org.hartford.GeminiDocAI.service.FileStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final FileStorageService storageService;
    private final DocumentProcessingService processingService;

    public DocumentController(FileStorageService storageService, DocumentProcessingService processingService) {
        this.storageService = storageService;
        this.processingService = processingService;
    }

    @PostMapping("/upload")
    public ResponseEntity<Object> upload(@RequestParam("files") List<MultipartFile> files) {
        if (files.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Empty file list"));
        }

        List<String> jobIds = new ArrayList<>();

        for (MultipartFile file : files) {
            String type = file.getContentType();
            if (type == null || !(type.equals("application/pdf") || type.startsWith("image/"))) {
                return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                        .body(Map.of("error", "Only PDF and Images are supported: " + file.getOriginalFilename()));
            }

            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(Map.of("error", "File too large: " + file.getOriginalFilename()));
            }

            try {
                String fileId = storageService.save(file);
                processingService.submitJob(fileId);
                jobIds.add(fileId);
            } catch (IOException e) {
                return ResponseEntity.internalServerError().body(Map.of("error", "Upload failed: " + e.getMessage()));
            }
        }

        return ResponseEntity.ok(Map.of("jobIds", jobIds));
    }

    @GetMapping("/status/{jobId}")
    public ResponseEntity<Object> status(@PathVariable String jobId) {
        var status = processingService.getStatus(jobId);
        if (status == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(status);
    }
}
