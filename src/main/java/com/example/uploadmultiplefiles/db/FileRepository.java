package com.example.uploadmultiplefiles.db;

import com.example.uploadmultiplefiles.model.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, String> {
    List<FileEntity> findByUserId(String userId);
}
