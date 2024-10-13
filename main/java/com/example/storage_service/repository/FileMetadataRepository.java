package com.example.storage_service.repository;

import com.example.storage_service.model.FileMetadata;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {

	List<FileMetadata> findByFileNameContaining(String fileName);
	//List<FileMetadata> findByFilenameContaining(String fileName);
}
