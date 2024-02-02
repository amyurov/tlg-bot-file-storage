package com.amyurov.service.impl;

import com.amyurov.entity.AppDocument;
import com.amyurov.entity.AppPhoto;
import com.amyurov.entity.BinaryContent;
import com.amyurov.repository.AppDocRepository;
import com.amyurov.repository.AppPhotoRepository;
import com.amyurov.service.FileService;
import lombok.extern.log4j.Log4j;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
@Log4j
public class FileServiceImpl implements FileService {

    private AppDocRepository appDocRepository;
    private AppPhotoRepository appPhotoRepository;

    public FileServiceImpl(AppDocRepository appDocRepository, AppPhotoRepository appPhotoRepository) {
        this.appDocRepository = appDocRepository;
        this.appPhotoRepository = appPhotoRepository;
    }

    @Override
    public AppDocument getDocument(String docId) {
        // TODO add decode hashed id
        Long id = Long.parseLong(docId);
        return appDocRepository.findById(id).orElse(null);
    }

    @Override
    public AppPhoto getPhoto(String photoId) {
        // TODO add decode hashed id
        Long id = Long.parseLong(photoId);
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
