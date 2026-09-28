package com.mehmetpekdemir.librarymanagementsystem.coverage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import com.mehmetpekdemir.librarymanagementsystem.controller.BookController;
import com.mehmetpekdemir.librarymanagementsystem.entity.Book;
import com.mehmetpekdemir.librarymanagementsystem.exception.NotFoundException;
import com.mehmetpekdemir.librarymanagementsystem.repository.BookRepository;
import com.mehmetpekdemir.librarymanagementsystem.service.AuthorService;
import com.mehmetpekdemir.librarymanagementsystem.service.BookService;
import com.mehmetpekdemir.librarymanagementsystem.service.CategoryService;
import com.mehmetpekdemir.librarymanagementsystem.service.PublisherService;
import com.mehmetpekdemir.librarymanagementsystem.service.impl.BookServiceImpl;

@ExtendWith(MockitoExtension.class)
class GraphCoverageTest {
    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookService bookServiceMock;

    @Mock
    private AuthorService authorService;

    @Mock
    private CategoryService categoryService;

    @Mock
    private PublisherService publisherService;

    @Mock
    private BindingResult bindingResult;

    private BookServiceImpl bookService;

    private BookController bookController;

    @BeforeEach
    void setUp() {
        bookService = new BookServiceImpl(bookRepository);
        bookController = new BookController(bookServiceMock, authorService, categoryService, publisherService);
    }

    @Test
    @DisplayName("searchBooks - test path [1,2]: keyword != null")
    void searchBooks_path_1_2() {
        List<Book> foundBooks = new ArrayList<>();
        when(bookRepository.search("java")).thenReturn(foundBooks);

        List<Book> result = bookService.searchBooks("java");

        assertSame(foundBooks, result);
        verify(bookRepository).search("java");
        verify(bookRepository, never()).findAll();
    }

    @Test
    @DisplayName("searchBooks - test path [1,3]: keyword == null")
    void searchBooks_path_1_3() {
        List<Book> allBooks = new ArrayList<>();
        when(bookRepository.findAll()).thenReturn(allBooks);

        List<Book> result = bookService.searchBooks(null);

        assertSame(allBooks, result);
        verify(bookRepository).findAll();
        verify(bookRepository, never()).search(anyString());
    }

    @Test
    @DisplayName("deleteBook - test path [1,2,4]: book exists")
    void deleteBook_path_1_2_4() {
        Book book = new Book("111", "Clean Code", "serial", "description");
        book.setId(7L);
        when(bookRepository.findById(7L)).thenReturn(Optional.of(book));

        bookService.deleteBook(7L);

        verify(bookRepository).deleteById(7L);
    }

    @Test
    @DisplayName("deleteBook - test path [1,2,3]: book does not exist")
    void deleteBook_path_1_2_3() {
        when(bookRepository.findById(8L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookService.deleteBook(8L));

        verify(bookRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("updateBook - test path [1,2]: binding errors")
    void updateBook_path_1_2() {
        Book book = new Book("111", "Clean Code", "serial", "description");
        Model model = new ExtendedModelMap();
        when(bindingResult.hasErrors()).thenReturn(true);

        String viewName = bookController.updateBook(5L, book, bindingResult, model);

        assertEquals("update-book", viewName);
        assertEquals(5L, book.getId());
        verify(bookServiceMock, never()).updateBook(any(Book.class));
    }

    @Test
    @DisplayName("updateBook - test path [1,3]: no errors")
    void updateBook_path_1_3() {
        Book book = new Book("111", "Clean Code", "serial", "description");
        book.setId(5L);
        Model model = new ExtendedModelMap();
        when(bindingResult.hasErrors()).thenReturn(false);

        String viewName = bookController.updateBook(5L, book, bindingResult, model);

        assertEquals("redirect:/books", viewName);
        verify(bookServiceMock).updateBook(book);
    }
}