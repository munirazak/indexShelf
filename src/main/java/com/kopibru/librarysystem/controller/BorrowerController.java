package com.kopibru.librarysystem.controller;

import com.kopibru.librarysystem.dto.BorrowerRegistrationRequest;
import com.kopibru.librarysystem.model.Borrower;
import com.kopibru.librarysystem.service.BorrowerService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/borrowers")
public class BorrowerController {

    private static final Logger log = LoggerFactory.getLogger(BorrowerController.class);

    private final BorrowerService borrowerService;

    public BorrowerController(BorrowerService borrowerService) {
        this.borrowerService = borrowerService;
    }

    @PostMapping
    public ResponseEntity<Borrower> registerBorrower(
            @Valid @RequestBody BorrowerRegistrationRequest request) {
        log.info("POST /api/borrowers libraryId={} username={}", request.getLibraryId(), request.getUsername());
        Borrower registered = borrowerService.register(request);
        log.info("Borrower registered id={} libraryId={}", registered.getId(), registered.getLibraryId());
        return ResponseEntity.status(HttpStatus.CREATED).body(registered);
    }
}
