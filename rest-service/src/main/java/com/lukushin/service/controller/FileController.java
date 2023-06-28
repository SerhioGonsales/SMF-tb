package com.lukushin.service.controller;

import com.lukushin.service.FileService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/file")
public class FileController {
    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("/get-doc")
    public ResponseEntity<?> getDocument(@RequestParam ("id") String id){
        var appDocument = fileService.getDocument(id);
        if(appDocument == null){
            return ResponseEntity.badRequest().build();
        }
        var biContent = appDocument.getBinaryContent();
        var fileSystemResource = fileService.getFileSystemResource(biContent);
        if(fileSystemResource == null){
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(appDocument.getMimeType()))
                .header("Content-disposition",
                        "attachment; filename=" + appDocument.getDocName())
                .body(fileSystemResource);
    }

    @GetMapping("/get-photo")
    public ResponseEntity<?> getPhoto(@RequestParam ("id") String id){
        var appPhoto = fileService.getPhoto(id);
        if(appPhoto == null){
            return ResponseEntity.badRequest().build();
        }
        var biContent = appPhoto.getBinaryContent();
        var fileSystemResource = fileService.getFileSystemResource(biContent);
        if(fileSystemResource == null){
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .header("Content-disposition",
                        "attachment;")
                .body(fileSystemResource);
    }
}
