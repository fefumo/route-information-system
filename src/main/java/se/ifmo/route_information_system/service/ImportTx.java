package se.ifmo.route_information_system.service;

import java.time.Instant;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import se.ifmo.route_information_system.model.ImportOperation;
import se.ifmo.route_information_system.model.ImportStatus;
import se.ifmo.route_information_system.repository.ImportRepository;

@Component
public class ImportTx {
    private final ImportRepository repo;

    public ImportTx(ImportRepository repo) {
        this.repo = repo;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ImportOperation saveNew(ImportOperation op) {
        return repo.saveAndFlush(op);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void finish(ImportOperation op, ImportStatus status, Integer addedCount, String errorMessage) {
        op.setStatus(status);
        op.setFinishedAt(Instant.now());
        op.setAddedCount(status == ImportStatus.SUCCESS ? addedCount : null);
        op.setErrorMessage(errorMessage);
        repo.saveAndFlush(op);
    }
}
