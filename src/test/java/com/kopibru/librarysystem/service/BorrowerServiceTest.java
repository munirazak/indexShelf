package com.kopibru.librarysystem.service;

import com.kopibru.librarysystem.client.UserManagementClient;
import com.kopibru.librarysystem.dto.BorrowerRegistrationRequest;
import com.kopibru.librarysystem.exception.DuplicateBorrowerException;
import com.kopibru.librarysystem.model.Borrower;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BorrowerServiceTest {

    @Mock
    private UserManagementClient userManagementClient;

    @InjectMocks
    private BorrowerService borrowerService;

    @Test
    void register_delegatesToUserManagement() {
        BorrowerRegistrationRequest request = new BorrowerRegistrationRequest(
                "LIB-001", "Alice", "alice@example.com", "alice", "password123");
        Borrower registered = new Borrower("LIB-001", "Alice", "alice@example.com");
        registered.setId(1L);
        when(userManagementClient.register(request)).thenReturn(registered);

        Borrower result = borrowerService.register(request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getLibraryId()).isEqualTo("LIB-001");
        verify(userManagementClient).register(request);
    }

    @Test
    void register_propagatesDuplicate() {
        BorrowerRegistrationRequest request = new BorrowerRegistrationRequest(
                "LIB-001", "Alice", "alice@example.com", "alice", "password123");
        when(userManagementClient.register(request))
                .thenThrow(new DuplicateBorrowerException("already exists"));

        assertThatThrownBy(() -> borrowerService.register(request))
                .isInstanceOf(DuplicateBorrowerException.class);
    }
}
