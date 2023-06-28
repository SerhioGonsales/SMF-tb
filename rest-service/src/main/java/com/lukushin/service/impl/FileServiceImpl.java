package com.lukushin.service.impl;

import com.lukushin.dao.AppDocumentDAO;
import com.lukushin.dao.AppPhotoDAO;
import com.lukushin.dao.BinaryContentDAO;
import com.lukushin.entity.AppDocument;
import com.lukushin.entity.AppPhoto;
import com.lukushin.entity.BinaryContent;
import com.lukushin.service.FileService;
import lombok.extern.log4j.Log4j;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
@Log4j
public class FileServiceImpl implements FileService {
    private final AppDocumentDAO appDocumentDAO;
    private final AppPhotoDAO appPhotoDAO;

    public FileServiceImpl(AppDocumentDAO appDocumentDAO,
                           AppPhotoDAO appPhotoDAO) {
        this.appDocumentDAO = appDocumentDAO;
        this.appPhotoDAO = appPhotoDAO;
    }

    @Override
    public AppDocument getDocument(String id) {
        Long docId = Long.parseLong(id);
        return appDocumentDAO.findById(docId).orElse(null);

    }

    @Override
    public AppPhoto getPhoto(String id) {
        Long photoId = Long.parseLong(id);
        return appPhotoDAO.findById(photoId).orElse(null);
    }

    @Override
    public FileSystemResource getFileSystemResource(BinaryContent binaryContent) {
        try {
            //TODO добавить генерацию имен временных файлов
            File temp = File.createTempFile("tempFile", "bin");
            temp.deleteOnExit();
            FileUtils.writeByteArrayToFile(temp, binaryContent.getFileAsArrayOfByte());
            return new FileSystemResource(temp);
        } catch (IOException e) {
            System.out.println("FileSystemResource");
            log.error(e);
            return null;
        }
    }
}
