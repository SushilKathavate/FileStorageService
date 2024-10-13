package com.example.storage_service.service;

import com.example.storage_service.exception.FileNotFoundException;
import com.example.storage_service.exception.FileStorageException;
import com.example.storage_service.model.FileMetadata;
import com.example.storage_service.repository.FileMetadataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;
    @Lazy
    @Autowired
    private FileMetadataRepository fileMetadataRepository;
    @Lazy
    @Autowired
    public FileStorageService() {
        this.fileStorageLocation = Paths.get("uploads")
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create directory", ex);
        }
    }
    
    public List<FileMetadata> searchFiles(String fileName) {
        return fileMetadataRepository.findByFileNameContaining(fileName);
    }

    public FileMetadata storeFile(MultipartFile file) {
        // Normalize file name
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // Check for invalid characters
            if (fileName.contains("..")) {
                throw new FileStorageException("Invalid path sequence " + fileName);
            }

            // Copy file to the target location
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Store file metadata
            FileMetadata metadata = new FileMetadata();
            metadata.setFileName(fileName);
            metadata.setFileSize(file.getSize());
            metadata.setFileType(file.getContentType());
            metadata.setUploadTime(LocalDateTime.now());
            metadata.setFileDownloadUri("/api/files/" + fileName);

            return fileMetadataRepository.save(metadata);
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + fileName, ex);
        }
    }


    public List<FileMetadata> getAllFiles() {
        return fileMetadataRepository.findAll();
    }

    public FileMetadata getFileById(Long id) {
        return fileMetadataRepository.findById(id)
                .orElseThrow(() -> new FileNotFoundException("File not found with id " + id));
    }

    public void deleteFile(Long id) {
        FileMetadata fileMetadata = getFileById(id);
        Path filePath = this.fileStorageLocation.resolve(fileMetadata.getFileName());

        try {
            Files.delete(filePath);
        } catch (IOException e) {
            throw new FileStorageException("Could not delete file with id " + id, e);
        }

        fileMetadataRepository.deleteById(id);
    }
}
