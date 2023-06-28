package com.lukushin.service;

import com.lukushin.entity.AppDocument;
import com.lukushin.entity.AppPhoto;
import com.lukushin.entity.BinaryContent;
import org.springframework.core.io.FileSystemResource;

public interface FileService {
    AppDocument getDocument(String id);
    AppPhoto getPhoto(String id);
    FileSystemResource getFileSystemResource(BinaryContent binaryContent);
}
