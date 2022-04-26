package com.example.uploadmultiplefiles.service;

import org.springframework.web.multipart.MultipartFile;

public interface ParseFileService {
    public void parse(MultipartFile file);
}
