package com.kopibru.librarysystem.client;

import com.kopibru.librarysystem.dto.BookDto;
import com.kopibru.librarysystem.exception.BookNotAvailableException;
import com.kopibru.librarysystem.exception.BookReturnException;
import com.kopibru.librarysystem.exception.InvalidBookStatusException;
import com.kopibru.librarysystem.exception.ResourceNotFoundException;
import com.kopibru.librarysystem.model.BookStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

class BookManagementClientTest {

    private MockRestServiceServer server;
    private BookManagementClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost:8081");
        server = MockRestServiceServer.bindTo(builder).build();
        client = new BookManagementClient(builder.build(), new ObjectMapper());
    }

    @Test
    void getBooks_returnsList() {
        server.expect(requestTo("http://localhost:8081/api/books?status=available&includeLibraryId=false"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        [{"id":"BOOK-001","isbn":"978-1","title":"Title","author":"Author","status":"AVAILABLE"}]
                        """, MediaType.APPLICATION_JSON));

        List<BookDto> books = client.getBooks("available");

        assertThat(books).hasSize(1);
        assertThat(books.get(0).getId()).isEqualTo("BOOK-001");
        assertThat(books.get(0).getStatus()).isEqualTo(BookStatus.AVAILABLE);
        server.verify();
    }

    @Test
    void getBooks_throwsInvalidStatusOnBadRequest() {
        server.expect(requestTo("http://localhost:8081/api/books?status=bad&includeLibraryId=false"))
                .andRespond(withBadRequest().body("""
                        {"error":"Invalid status 'bad'"}
                        """).contentType(MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.getBooks("bad"))
                .isInstanceOf(InvalidBookStatusException.class)
                .hasMessageContaining("Invalid status");
        server.verify();
    }

    @Test
    void borrow_returnsBook() {
        server.expect(requestTo("http://localhost:8081/api/books/BOOK-001/borrow"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("""
                        {"id":"BOOK-001","isbn":"978-1","title":"Title","author":"Author","status":"BORROWED","libraryId":"LIB-001"}
                        """, MediaType.APPLICATION_JSON));

        BookDto result = client.borrow("BOOK-001", "LIB-001");

        assertThat(result.getStatus()).isEqualTo(BookStatus.BORROWED);
        assertThat(result.getLibraryId()).isEqualTo("LIB-001");
        server.verify();
    }

    @Test
    void borrow_throwsNotFound() {
        server.expect(requestTo("http://localhost:8081/api/books/BOOK-001/borrow"))
                .andRespond(withStatus(NOT_FOUND).body("""
                        {"error":"Book copy with id 'BOOK-001' not found"}
                        """).contentType(MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.borrow("BOOK-001", "LIB-001"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("BOOK-001");
        server.verify();
    }

    @Test
    void borrow_throwsNotAvailableOnConflict() {
        server.expect(requestTo("http://localhost:8081/api/books/BOOK-001/borrow"))
                .andRespond(withStatus(CONFLICT).body("""
                        {"error":"Book copy with id 'BOOK-001' is not available"}
                        """).contentType(MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.borrow("BOOK-001", "LIB-001"))
                .isInstanceOf(BookNotAvailableException.class)
                .hasMessageContaining("not available");
        server.verify();
    }

    @Test
    void returnBook_returnsBook() {
        server.expect(requestTo("http://localhost:8081/api/books/BOOK-001/return"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("""
                        {"id":"BOOK-001","isbn":"978-1","title":"Title","author":"Author","status":"AVAILABLE"}
                        """, MediaType.APPLICATION_JSON));

        BookDto result = client.returnBook("BOOK-001", "LIB-001");

        assertThat(result.getStatus()).isEqualTo(BookStatus.AVAILABLE);
        server.verify();
    }

    @Test
    void returnBook_throwsReturnExceptionOnConflict() {
        server.expect(requestTo("http://localhost:8081/api/books/BOOK-001/return"))
                .andRespond(withStatus(CONFLICT).body("""
                        {"error":"Book copy with id 'BOOK-001' is not borrowed by libraryId 'LIB-001'"}
                        """).contentType(MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.returnBook("BOOK-001", "LIB-001"))
                .isInstanceOf(BookReturnException.class)
                .hasMessageContaining("not borrowed");
        server.verify();
    }

    @Test
    void borrow_throwsIllegalStateOnServerError() {
        server.expect(requestTo("http://localhost:8081/api/books/BOOK-001/borrow"))
                .andRespond(withServerError().body("boom"));

        assertThatThrownBy(() -> client.borrow("BOOK-001", "LIB-001"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("BookManagement error");
        server.verify();
    }
}
