package com.collabera.librarysystem.controller;

import com.collabera.librarysystem.dto.BorrowerRegistrationRequest;
import com.collabera.librarysystem.model.Borrower;
import com.collabera.librarysystem.service.BorrowerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/borrowers")
public class BorrowerController {

    private final BorrowerService borrowerService;

    public BorrowerController(BorrowerService borrowerService) {
        this.borrowerService = borrowerService;
    }

    @PostMapping
    public ResponseEntity<Borrower> registerBorrower(
            @Valid @RequestBody BorrowerRegistrationRequest request) {
        Borrower registered = borrowerService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(registered);
    }

    @GetMapping
    public ResponseEntity<List<Borrower>> getAllBorrowers() {
        return ResponseEntity.ok(borrowerService.getAllBorrowers());
    }
}
