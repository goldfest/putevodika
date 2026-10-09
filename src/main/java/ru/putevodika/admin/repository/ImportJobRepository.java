package ru.putevodika.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.putevodika.admin.entity.ImportJob;

public interface ImportJobRepository extends JpaRepository<ImportJob, Long>,
        JpaSpecificationExecutor<ImportJob> {
}
