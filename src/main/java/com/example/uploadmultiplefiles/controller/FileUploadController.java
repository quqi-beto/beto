package com.example.uploadmultiplefiles.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.example.uploadmultiplefiles.model.FileEntity;
import com.example.uploadmultiplefiles.model.FileResponse;
import com.example.uploadmultiplefiles.model.OrderSlip;
import com.example.uploadmultiplefiles.service.FileService;
import com.example.uploadmultiplefiles.service.FilesStorageServiceImpl;
import com.example.uploadmultiplefiles.singleton.UserOrdersFile;
import com.example.uploadmultiplefiles.util.AppUtil;
import com.example.uploadmultiplefiles.util.FileGeneratorUtil;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("file")
@CrossOrigin(origins = {"https://spring-crud-master.herokuapp.com","localhost:4200"})
public class FileUploadController {
    @Autowired
    private final FileService fileService;
    @Autowired
    private FilesStorageServiceImpl filesStorageService;

    @Autowired
    public FileUploadController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping
    public ResponseEntity<String> upload(@RequestParam("userId") String userId, @RequestParam("file") MultipartFile file) {
        try {
            if(file.getContentType().equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")){
//                fileService.save(file,userId);
                UserOrdersFile.getInstance().remove(userId);
                UserOrdersFile.getInstance().put(userId,file.getBytes());
            }else if(file.getContentType().equals("application/pdf")){

                List<OrderSlip> orderList = getOrderList(userId, 1);

                List<PDDocument> pdDocumentList = AppUtil.splitPdf(file);

                if(!AppUtil.validate(orderList, pdDocumentList)) {
                    return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
                            .body(String.format("PDF and Excel file doesn't match."));
                }

                FileGeneratorUtil.combined(orderList,file.getOriginalFilename());

                filesStorageService.cleanUp();
            }else{
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
                        .body(String.format("Invalid file type: %s!", file.getOriginalFilename()));
            }

            return ResponseEntity.status(HttpStatus.OK)
                    .body(String.format("File uploaded successfully: %s", file.getOriginalFilename()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(String.format("Could not upload the file: %s!", file.getOriginalFilename()));
        }
    }

//    @PostMapping
//    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) {
//        try {
//            fileService.save(file);
//
//            return ResponseEntity.status(HttpStatus.OK)
//                    .body(String.format("File uploaded successfully: %s", file.getOriginalFilename()));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(String.format("Could not upload the file: %s!", file.getOriginalFilename()));
//        }
//    }

    @GetMapping
    public List<FileResponse> list() {
        return fileService.getAllFiles()
                .stream()
                .map(this::mapToFileResponse)
                .collect(Collectors.toList());
    }

    private FileResponse mapToFileResponse(FileEntity fileEntity) {
        String downloadURL = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/file/")
                .path(fileEntity.getId())
                .toUriString();
        FileResponse fileResponse = new FileResponse();
        fileResponse.setId(fileEntity.getId());
        fileResponse.setName(fileEntity.getName());
        fileResponse.setContentType(fileEntity.getContentType());
        fileResponse.setSize(fileEntity.getSize());
        fileResponse.setUrl(downloadURL);

        return fileResponse;
    }

    @GetMapping("{id}")
    public ResponseEntity<byte[]> getFile(@PathVariable String id) {
        Optional<FileEntity> fileEntityOptional = fileService.getFile(id);

        if (!fileEntityOptional.isPresent()) {
            return ResponseEntity.notFound()
                    .build();
        }

        FileEntity fileEntity = fileEntityOptional.get();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileEntity.getName() + "\"")
                .contentType(MediaType.valueOf(fileEntity.getContentType()))
                .body(fileEntity.getData());
    }

    @DeleteMapping()
    public ResponseEntity<String> deleteAll(){
        try {
            fileService.deleteAllFiles();
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(String.format("Files successfully deleted."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(String.format("Deleting all files failed."));
        }
    }

    private List<OrderSlip> getOrderList(String userId, int nextBagNumber) throws IOException {
            long startOrderList = System.nanoTime();
            InputStream myInputStream = new ByteArrayInputStream(UserOrdersFile.getInstance().get(userId));
            long stopOrderList = System.nanoTime();
            System.out.println("OrderList time:" + (stopOrderList - startOrderList));
        return AppUtil.getOrderList(myInputStream, nextBagNumber);
    }
}
