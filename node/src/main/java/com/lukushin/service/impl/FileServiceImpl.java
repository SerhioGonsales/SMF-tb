package com.lukushin.service.impl;

import com.lukushin.dao.AppDocumentDAO;
import com.lukushin.dao.AppPhotoDAO;
import com.lukushin.dao.BinaryContentDAO;
import com.lukushin.entity.AppDocument;
import com.lukushin.entity.AppPhoto;
import com.lukushin.entity.BinaryContent;
import com.lukushin.exceptions.UploadFileException;
import com.lukushin.service.FileService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

@Service
public class FileServiceImpl implements FileService {
    @Value("${token}")
    private String token;
    @Value("${service.file_info.uri}")
    private String fileInfoUri;
    @Value("${service.file_storage.uri}")
    private String fileStorageUri;

    private final BinaryContentDAO binaryContentDAO;
    private final AppDocumentDAO appDocumentDAO;
    private final AppPhotoDAO appPhotoDAO;

    public FileServiceImpl(BinaryContentDAO binaryContentDAO,
                           AppDocumentDAO appDocumentDAO,
                           AppPhotoDAO appPhotoDAO) {
        this.binaryContentDAO = binaryContentDAO;
        this.appDocumentDAO = appDocumentDAO;
        this.appPhotoDAO = appPhotoDAO;
    }

    @Override
    public AppDocument processDoc(Message telegramMessage) {
        String fileId = telegramMessage.getDocument().getFileId();
        ResponseEntity<String> response = getFilePath(fileId);
        if(response.getStatusCode() == HttpStatus.OK){
            var persistenceBinaryContent = getPersistentBiContent(response);
            var document = telegramMessage.getDocument();
            return buildAppDocument(document, persistenceBinaryContent);
        } else {
            throw new UploadFileException("Bad response from telegram service: " + response);
        }
    }

    @Override
    public AppPhoto processPhoto(Message telegramMessage) {
        var photoSizeCount = telegramMessage.getPhoto().size();
        var photoIndex = photoSizeCount > 1 ? photoSizeCount -1 : 0;
        PhotoSize telegramPhoto = telegramMessage.getPhoto().get(photoIndex);
        var fileId = telegramPhoto.getFileId();
        ResponseEntity<String> response = getFilePath(fileId);
        if(response.getStatusCode() == HttpStatus.OK){
            var persistenceBinaryContent = getPersistentBiContent(response);
            return buildAppPhoto(telegramPhoto, persistenceBinaryContent);
        } else {
            throw new UploadFileException("Bad response from telegram service: " + response);
        }
    }

    private BinaryContent getPersistentBiContent(ResponseEntity<String> response){
        JSONObject jsonObject = new JSONObject(Objects.requireNonNull(response.getBody()));
        var filePath = String.valueOf(jsonObject
                .getJSONObject("result")
                .getString("file_path"));
        byte[] fileInByte = downloadFile(filePath);
        BinaryContent transientBinaryContent = BinaryContent.builder()
                .fileAsArrayOfByte(fileInByte)
                .build();
        return binaryContentDAO.save(transientBinaryContent);
    }

    private AppDocument buildAppDocument(Document document, BinaryContent biContent){
        var transientAppDocument = AppDocument.builder()
                .telegramFileId(document.getFileId())
                .docName(document.getFileName())
                .binaryContent(biContent)
                .mimeType(document.getMimeType())
                .fileSize(document.getFileSize())
                .build();
        return appDocumentDAO.save(transientAppDocument);
    }

    private AppPhoto buildAppPhoto(PhotoSize telegramPhoto, BinaryContent biContent) {
        var transientAppPhoto = AppPhoto.builder()
                .telegramFileId(telegramPhoto.getFileId())
                .binaryContent(biContent)
                .fileSize(telegramPhoto.getFileSize())
                .build();
        return appPhotoDAO.save(transientAppPhoto);
    }

    private ResponseEntity<String> getFilePath(String fileId) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> request = new HttpEntity<>(headers);

        return restTemplate.exchange(
                fileInfoUri,
                HttpMethod.GET,
                request,
                String.class,
                token,
                fileId);
     }

    private byte[] downloadFile(String filePath) {
        String fullUri = fileStorageUri.replace("{token}", token)
                .replace("{filePath}", filePath);
        URL uri;
        try {
            uri = new URL(fullUri);
        } catch (MalformedURLException e) {
            throw new UploadFileException(e);
        }

        //TODO подумать над оптимизацией
        try(InputStream inputStream = uri.openStream()){
            return inputStream.readAllBytes();
        } catch (IOException e) {
            throw new UploadFileException(uri.toExternalForm(), e);
        }
    }
}
