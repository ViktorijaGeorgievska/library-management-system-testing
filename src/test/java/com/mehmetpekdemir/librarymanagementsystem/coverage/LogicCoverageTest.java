package com.mehmetpekdemir.librarymanagementsystem.coverage;

import com.mehmetpekdemir.librarymanagementsystem.entity.Book;
import com.mehmetpekdemir.librarymanagementsystem.repository.BookRepository;
import com.mehmetpekdemir.librarymanagementsystem.service.BookService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
public class LogicCoverageTest {
    @MockBean
    private BookService bookService;

    @Autowired // Spring автоматски внесува (inject) објект што е потребен (автоматски поврзи ја потребната зависност)
    private BookRepository bookRepository;

    private static final String KEYWORD = "java";

    private void saveBook(String isbn, String name, String serialName) {
        bookRepository.save(new Book(isbn, name, serialName, "description"));
    }

    @Test
    @DisplayName("TC1: a=T, b=F, c=F -> p=T (keyword only in name)")
    void keywordOnlyInName_bookIsFound() {
        saveBook("ISBN-001", "learning java", "alpha series");

        List<Book> result = bookRepository.search(KEYWORD);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("TC2: a=F, b=F, c=F -> p=F (keyword nowhere)")
    void keywordNowhere_bookIsNotFound() {
        saveBook("ISBN-002", "clean code", "beta series");

        List<Book> result = bookRepository.search(KEYWORD);

        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("TC3: a=F, b=T, c=F -> p=T (keyword only in isbn)")
    void keywordOnlyInIsbn_bookIsFound() {
        saveBook("java-003", "clean code", "gamma series");

        List<Book> result = bookRepository.search(KEYWORD);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("TC4: a=F, b=F, c=T -> p=T (keyword only in serialName)")
    void keywordOnlyInSerialName_bookIsFound() {
        saveBook("ISBN-004", "clean code", "java series");

        List<Book> result = bookRepository.search(KEYWORD);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("TC5: a=T, b=T, c=T -> p=T (book is returned only once)")
    void keywordInAllFields_bookIsFoundOnce() {
        saveBook("java-005", "java basics", "java series");

        List<Book> result = bookRepository.search(KEYWORD);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Boundary value: empty keyword (LIKE '%%') returns all books")
    void emptyKeyword_returnsAllBooks() {
        saveBook("ISBN-006", "clean code", "beta series");
        saveBook("ISBN-007", "refactoring", "delta series");

        List<Book> result = bookRepository.search("");

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Search is case sensitive")
    void searchIsCaseSensitive() {
        saveBook("ISBN-008", "learning java", "alpha series");

        List<Book> result = bookRepository.search("JAVA");

        assertEquals(0, result.size());
    }

}
