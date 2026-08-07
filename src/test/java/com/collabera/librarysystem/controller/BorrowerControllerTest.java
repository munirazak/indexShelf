package com.collabera.librarysystem.controller;

import com.collabera.librarysystem.dto.BorrowerRegistrationRequest;
import com.collabera.librarysystem.exception.DuplicateBorrowerException;
import com.collabera.librarysystem.exception.GlobalExceptionHandler;
import com.collabera.librarysystem.model.Borrower;
import com.collabera.librarysystem.service.BorrowerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BorrowerController.class)
@Import(GlobalExceptionHandler.class)
class BorrowerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BorrowerService borrowerService;

    @Test
    void registerBorrower_returnsCreated() throws Exception {
        BorrowerRegistrationRequest request = new BorrowerRegistrationRequest(
                "LIB-001", "Alice", "alice@example.com", "alice", "password123");
        Borrower borrower = new Borrower("LIB-001", "Alice", "alice@example.com");
        borrower.setId(1L);
        when(borrowerService.register(any(BorrowerRegistrationRequest.class))).thenReturn(borrower);

        mockMvc.perform(post("/api/borrowers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.libraryId").value("LIB-001"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void registerBorrower_returnsBadRequestWhenInvalid() throws Exception {
        String body = """
                {"libraryId":"","name":"Alice","email":"bad-email","username":"ab","password":"short"}
                """;

        mockMvc.perform(post("/api/borrowers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").exists());
    }

    @Test
    void registerBorrower_returnsConflictWhenDuplicate() throws Exception {
        BorrowerRegistrationRequest request = new BorrowerRegistrationRequest(
                "LIB-001", "Alice", "alice@example.com", "alice", "password123");
        when(borrowerService.register(any(BorrowerRegistrationRequest.class)))
                .thenThrow(new DuplicateBorrowerException("already exists"));

        mockMvc.perform(post("/api/borrowers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void getAllBorrowers_returnsOk() throws Exception {
        when(borrowerService.getAllBorrowers()).thenReturn(List.of(
                new Borrower("LIB-001", "Alice", "alice@example.com")));

        mockMvc.perform(get("/api/borrowers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].libraryId").value("LIB-001"));
    }
}
