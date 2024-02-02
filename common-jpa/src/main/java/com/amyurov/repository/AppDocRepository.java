package com.amyurov.repository;

import com.amyurov.entity.AppDocument;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppDocRepository extends JpaRepository<AppDocument, Long> {

}
