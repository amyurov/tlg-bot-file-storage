package com.amyurov.repository;

import com.amyurov.entity.AppDocument;
import com.amyurov.entity.AppPhoto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppPhotoRepository extends JpaRepository<AppPhoto, Long> {

}
