package com.amyurov.service.impl;

import com.amyurov.entity.AppDocument;
import com.amyurov.entity.AppPhoto;
import com.amyurov.entity.BinaryContent;
import com.amyurov.exception.UploadFileException;
import com.amyurov.repository.AppDocRepository;
import com.amyurov.repository.AppPhotoRepository;
import com.amyurov.repository.BinaryContentRepository;
import com.amyurov.service.FileService;
import lombok.extern.log4j.Log4j;
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

@Service
@Log4j
public class FileServiceImpl implements FileService {

    @Value("${token}")
    private String token;
    @Value("${service.file_info.uri}")
    private String fileInfoUri;
    @Value("${service.file_storage.uri}")
    private String fileStorageUri;

    private AppDocRepository appDocRepository;
    private AppPhotoRepository appPhotoRepository;
    private BinaryContentRepository binaryContentRepository;

    public FileServiceImpl(AppDocRepository appDocRepository, AppPhotoRepository appPhotoRepository,
            BinaryContentRepository binaryContentRepository) {
        this.appDocRepository = appDocRepository;
        this.appPhotoRepository = appPhotoRepository;
        this.binaryContentRepository = binaryContentRepository;
    }

    @Override
    public AppDocument processDoc(Message telegramMessage) {
        Document telegramDoc = telegramMessage.getDocument();
        String fileId = telegramDoc.getFileId();
        ResponseEntity<String> response = getFilePath(fileId);
        if (response.getStatusCode() == HttpStatus.OK) {
            BinaryContent persistentBinaryContent = getPersistentBinaryContent(response);
            Document telegramDocument = telegramMessage.getDocument();
            AppDocument transientAppDoc = buildTransientAppDoc(telegramDocument, persistentBinaryContent);
            return appDocRepository.save(transientAppDoc);
        } else {
            throw new UploadFileException("Bad response from tlg: " + response);
        }
    }

    @Override
    public AppPhoto processPhoto(Message telegramMessage) {
        // TODO case works only for 1 uploaded photo
        PhotoSize telegramPhoto = telegramMessage.getPhoto().get(0);
        String fileId = telegramPhoto.getFileId();
        ResponseEntity<String> response = getFilePath(fileId);
        if (response.getStatusCode() == HttpStatus.OK) {
            BinaryContent persistentBinaryContent = getPersistentBinaryContent(response);
            AppPhoto transientAppPhoto = buildTransientAppPhoto(telegramPhoto, persistentBinaryContent);
            return appPhotoRepository.save(transientAppPhoto);
        } else {
            throw new UploadFileException("Bad response from tlg: " + response);
        }
    }

    private BinaryContent getPersistentBinaryContent(ResponseEntity<String> response) {
        String filePath = getFilePath(response);
        byte[] fileInByte = downloadFile(filePath);
        BinaryContent transientBinaryContent = BinaryContent.builder().fileAsArrayOfByte(fileInByte).build();
        return binaryContentRepository.save(transientBinaryContent);
    }

    private static String getFilePath(ResponseEntity<String> response) {
        JSONObject jsonObject = new JSONObject(response.getBody());
        return String.valueOf(jsonObject.getJSONObject("result").getString("file_path"));
    }

    private AppDocument buildTransientAppDoc(Document telegramDocument, BinaryContent persistentBinaryContent) {
        return AppDocument.builder()
                .telegramFileId(telegramDocument.getFileId())
                .docName(telegramDocument.getFileName())
                .binaryContent(persistentBinaryContent)
                .mimeType(telegramDocument.getMimeType())
                .fileSize(telegramDocument.getFileSize())
                .build();
    }

    private AppPhoto buildTransientAppPhoto(PhotoSize telegramPhoto, BinaryContent persistentBinaryContent) {
        return AppPhoto.builder()
                .telegramFileId(telegramPhoto.getFileId())
                .binaryContent(persistentBinaryContent)
                .fileSize(telegramPhoto.getFileSize())
                .build();
    }

    private byte[] downloadFile(String filePath) {
        String fullUri = fileStorageUri.replace("{token}", token).replace("{filePath}", filePath);

        URL urlObj;
        try {
            urlObj = new URL(fullUri);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        try (InputStream inputStream = urlObj.openStream();) {
            return inputStream.readAllBytes();
        } catch (IOException e) {
            throw new UploadFileException(urlObj.toExternalForm(), e);
        }
    }

    private ResponseEntity<String> getFilePath(String fileId) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        HttpEntity<String> request = new HttpEntity<>(httpHeaders);

        return restTemplate.exchange(fileInfoUri, HttpMethod.GET, request, String.class, token, fileId);
    }
}
