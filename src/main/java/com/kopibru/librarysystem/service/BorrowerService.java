package com.kopibru.librarysystem.service;

import com.kopibru.librarysystem.client.UserManagementClient;
import com.kopibru.librarysystem.dto.BorrowerRegistrationRequest;
import com.kopibru.librarysystem.model.Borrower;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class BorrowerService {

    private static final Logger log = LoggerFactory.getLogger(BorrowerService.class);

    private final UserManagementClient userManagementClient;

    public BorrowerService(UserManagementClient userManagementClient) {
        this.userManagementClient = userManagementClient;
    }

    public Borrower register(BorrowerRegistrationRequest request) {
        log.info("Registering borrower via UserManagement libraryId={} username={}",
                request.getLibraryId(), request.getUsername());
        Borrower registered = userManagementClient.register(request);
        log.info("Borrower registered id={} libraryId={}", registered.getId(), registered.getLibraryId());
        return registered;
    }
}
