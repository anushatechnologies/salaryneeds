package com.salaryneeds.repository;

import com.salaryneeds.entity.WorkerDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkerDocumentRepository extends JpaRepository<WorkerDocument, String> {
    List<WorkerDocument> findByWorkerIdOrderByUploadedAtDesc(String workerId);
}
