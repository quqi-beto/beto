package com.example.uploadmultiplefiles.service;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@Service
public class FilesStorageServiceImpl implements FilesStorageService {
    private final Path root = Paths.get("uploads");
    private final Path saved = Paths.get("saved");
    @Override
    public void init() {
        try {
            if(!Files.exists(root))
                Files.createDirectory(root);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize folder for upload!");
        }
    }
    @Override
    public void save(MultipartFile file) {
        try {
            Files.copy(file.getInputStream(), this.root.resolve(file.getOriginalFilename()));
        } catch (Exception e) {
            throw new RuntimeException("Could not store the file. Error: " + e.getMessage());
        }
    }
    @Override
    public Resource load(String filename) {
        try {
            Path file = saved.resolve(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read the file!");
            }
        } catch (IOException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }
    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(root.toFile());
        FileSystemUtils.deleteRecursively(saved.toFile());
        init();
    }

    @Override
    public void cleanUp() {
        FileSystemUtils.deleteRecursively(root.toFile());
        init();
    }

    @Override
    public void deleteSaved() throws IOException {
        FileSystemUtils.deleteRecursively(saved.toFile());
        if(!Files.exists(saved))
            Files.createDirectory(saved);
    }

    @Override
    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.saved, 1).filter(path -> !path.equals(this.saved)).map(this.saved::relativize);
        } catch (IOException e) {
            throw new RuntimeException("Could not load the files!");
        }
    }
}
