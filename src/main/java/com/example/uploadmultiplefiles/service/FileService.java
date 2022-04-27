package com.example.uploadmultiplefiles.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import com.example.uploadmultiplefiles.db.FileRepository;
import com.example.uploadmultiplefiles.model.FileEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {

    private final FileRepository fileRepository;

    @Autowired
    public FileService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    public void save(MultipartFile file, String userId) throws IOException {
        FileEntity fileEntity = new FileEntity();
        fileEntity.setName(StringUtils.cleanPath(file.getOriginalFilename()));
        fileEntity.setContentType(file.getContentType());
        fileEntity.setData(file.getBytes());
        fileEntity.setSize(file.getSize());
        fileEntity.setUserId(userId);

        fileRepository.save(fileEntity);
    }

    public Optional<FileEntity> getFile(String id) {
        return fileRepository.findById(id);
    }

    public List<FileEntity> getAllFiles() {
        return fileRepository.findAll();
    }

    public void deleteAllFiles() {
        fileRepository.deleteAll();
    }

    @Transactional
    public FileEntity getFileByUserId(String userId) {
        List<FileEntity> list = fileRepository.findByUserId(userId);
        return list.get(0);
    }
}