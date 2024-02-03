package com.amyurov.service.impl;

import com.amyurov.entity.AppDocument;
import com.amyurov.entity.AppPhoto;
import com.amyurov.entity.BinaryContent;
import com.amyurov.repository.AppDocRepository;
import com.amyurov.repository.AppPhotoRepository;
import com.amyurov.service.FileService;
import com.amyurov.utils.CryptoTool;
import lombok.extern.log4j.Log4j;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
@Log4j
public class FileServiceImpl implements FileService {

    private final AppDocRepository appDocRepository;
    private final AppPhotoRepository appPhotoRepository;
    private final CryptoTool cryptoTool;

    public FileServiceImpl(AppDocRepository appDocRepository, AppPhotoRepository appPhotoRepository,
            CryptoTool cryptoTool) {
        this.appDocRepository = appDocRepository;
        this.appPhotoRepository = appPhotoRepository;
        this.cryptoTool = cryptoTool;
    }

    @Override
    public AppDocument getDocument(String docId) {
        Long id = cryptoTool.idOf(docId);
        if (id == null) {
            return null;
        }
        return appDocRepository.findById(id).orElse(null);
    }

    @Override
    public AppPhoto getPhoto(String photoId) {
        Long id = cryptoTool.idOf(photoId);
        if (id == null) {
            return null;
        }
        return appPhotoRepository.findById(id).orElse(null);
    }

    @Override
    public FileSystemResource getFileSystemResource(BinaryContent binaryContent) {
        try {
            // TODO evade potential temp files names collisions
            File temp = File.createTempFile("tempFile", ".bin");
            temp.deleteOnExit();
            FileUtils.writeByteArrayToFile(temp, binaryContent.getFileAsArrayOfByte());
            return new FileSystemResource(temp);
        } catch (IOException e) {
            log.error(e);
            return null;
        }
    }
}
