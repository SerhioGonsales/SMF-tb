package com.lukushin.service.impl;

import com.lukushin.dao.AppDocumentDAO;
import com.lukushin.dao.BinaryContentDAO;
import com.lukushin.entity.AppDocument;
import com.lukushin.entity.BinaryContent;
import com.lukushin.exceptions.UploadFileException;
import com.lukushin.service.FileService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.objects.Message;

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

    public FileServiceImpl(BinaryContentDAO binaryContentDAO, AppDocumentDAO appDocumentDAO) {
        this.binaryContentDAO = binaryContentDAO;
        this.appDocumentDAO = appDocumentDAO;
    }

    @Override
    public AppDocument processDoc(Message telegramMessage) {
        String fileId = telegramMessage.getDocument().getFileId();
        ResponseEntity<String> response = getFilePath(fileId);
        if(response.getStatusCode() == HttpStatus.OK){
            JSONObject jsonObject = new JSONObject(Objects.requireNonNull(response.getBody()));
            var filePath = String.valueOf(jsonObject
                    .getJSONObject("result")
                    .getString("file_path"));
            byte[] fileInByte = downloadFile(filePath);
            BinaryContent transientBinaryContent = BinaryContent.builder()
                    .fileAsArrayOfByte(fileInByte)
                    .build();
            var persistenceBinaryContent = binaryContentDAO.save(transientBinaryContent);
            var document = telegramMessage.getDocument();
            var transientAppDocument = AppDocument.builder()
                    .telegramFileId(document.getFileId())
                    .docName(document.getFileName())
                    .binaryContent(persistenceBinaryContent)
                    .mimeType(document.getMimeType())
                    .fileSize(document.getFileSize())
                    .build();
            return appDocumentDAO.save(transientAppDocument);
        } else {
            throw new UploadFileException("Bad response from telegram service: " + response);
        }
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
