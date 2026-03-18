package org.hartford.GeminiDocAI.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path root = Paths.get("uploads");

    public FileStorageService() throws IOException {
        if (!Files.exists(root)) {
            Files.createDirectory(root);
        }
    }

    public String save(MultipartFile file) throws IOException {
        String fileId = UUID.randomUUID().toString();
        String extension = getExtension(file.getOriginalFilename());
        String fileName = fileId + (extension.isEmpty() ? "" : "." + extension);
        Files.copy(file.getInputStream(), this.root.resolve(fileName));
        return fileId;
    }

    public Path load(String fileId) throws IOException {
        return Files.list(root)
                .filter(path -> path.getFileName().toString().startsWith(fileId))
                .findFirst()
                .orElseThrow(() -> new IOException("File not found: " + fileId));
    }

    private String getExtension(String fileName) {
        if (fileName == null) return "";
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
    }
}
