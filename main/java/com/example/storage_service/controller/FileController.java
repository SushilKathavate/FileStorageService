package com.example.storage_service.controller;

import com.example.storage_service.model.FileMetadata;
import com.example.storage_service.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileController {
    @Lazy
    @Autowired
    private FileStorageService fileStorageService;
    
    @GetMapping("/search")
    public List<FileMetadata> searchFiles(@RequestParam String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("File name must not be empty");
        }
        return fileStorageService.searchFiles(fileName);
    }

    
    @PostMapping("/upload")
    public FileMetadata uploadFile(@RequestParam("file") MultipartFile file) {
        return fileStorageService.storeFile(file);
    }

    @GetMapping
    public List<FileMetadata> getAllFiles() {
        return fileStorageService.getAllFiles();
    }

    @GetMapping("/{id}")
    public FileMetadata getFileById(@PathVariable Long id) {
        return fileStorageService.getFileById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteFile(@PathVariable Long id) {
        fileStorageService.deleteFile(id);
    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String fileName) throws Exception {
        Path filePath = Paths.get("uploads").resolve(fileName).normalize();
        byte[] fileContent = Files.readAllBytes(filePath);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=" + fileName);

        return ResponseEntity.ok().headers(headers).body(fileContent);
    }
}
