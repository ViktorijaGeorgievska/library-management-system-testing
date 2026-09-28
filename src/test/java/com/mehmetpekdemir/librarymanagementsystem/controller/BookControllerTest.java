package com.mehmetpekdemir.librarymanagementsystem.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.util.NestedServletException;

import com.mehmetpekdemir.librarymanagementsystem.entity.Book;
import com.mehmetpekdemir.librarymanagementsystem.exception.NotFoundException;
import com.mehmetpekdemir.librarymanagementsystem.service.AuthorService;
import com.mehmetpekdemir.librarymanagementsystem.service.BookService;
import com.mehmetpekdemir.librarymanagementsystem.service.CategoryService;
import com.mehmetpekdemir.librarymanagementsystem.service.PublisherService;

@ExtendWith(MockitoExtension.class)
class BookControllerTest {
    @Mock
    private BookService bookService;

    @Mock
    private AuthorService authorService;

    @Mock
    private CategoryService categoryService;

    @Mock
    private PublisherService publisherService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        BookController bookController = new BookController(bookService, authorService, categoryService,
                publisherService);

        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/");
        viewResolver.setSuffix(".html");

        mockMvc = MockMvcBuilders.standaloneSetup(bookController).setViewResolvers(viewResolver).build();
    }

    private Book createTestBook(Long id, String isbn, String name) {
        Book book = new Book(isbn, name, "Test serial", "Test description");
        book.setId(id);
        return book;
    }

    @Test
    @DisplayName("GET /books shows the list with all books")
    void getBooks() throws Exception {
        List<Book> books = new ArrayList<>();
        books.add(createTestBook(1L, "111", "Clean Code"));
        books.add(createTestBook(2L, "222", "Refactoring"));
        when(bookService.findAllBooks()).thenReturn(books);

        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(view().name("list-books"))
                .andExpect(model().attribute("books", books));
    }

    @Test
    @DisplayName("GET /searchBook?keyword=... returns the found books and the keyword")
    void searchBookWithKeyword() throws Exception {
        List<Book> foundBooks = new ArrayList<>();
        foundBooks.add(createTestBook(1L, "111", "Clean Code"));
        when(bookService.searchBooks("Clean")).thenReturn(foundBooks);

        mockMvc.perform(get("/searchBook").param("keyword", "Clean"))
                .andExpect(status().isOk())
                .andExpect(view().name("list-books"))
                .andExpect(model().attribute("books", foundBooks))
                .andExpect(model().attribute("keyword", "Clean"));

        verify(bookService).searchBooks("Clean");
    }

    @Test
    @DisplayName("GET /searchBook without keyword passes null to the service")
    void searchBookWithoutKeyword() throws Exception {
        List<Book> allBooks = new ArrayList<>();
        when(bookService.searchBooks(null)).thenReturn(allBooks);

        mockMvc.perform(get("/searchBook"))
                .andExpect(status().isOk())
                .andExpect(view().name("list-books"))
                .andExpect(model().attribute("keyword", nullValue()));

        verify(bookService).searchBooks(null);
    }

    @Test
    @DisplayName("GET /book/{id} shows the book details")
    void getBookByExistingId() throws Exception {
        Book book = createTestBook(1L, "111", "Clean Code");
        when(bookService.findBookById(1L)).thenReturn(book);

        mockMvc.perform(get("/book/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("list-book"))
                .andExpect(model().attribute("book", book));
    }

    @Test
    @DisplayName("GET /book/{id} for non-existing book: exception is not handled")
    void getBookByNonExistingId() {
        when(bookService.findBookById(99L)).thenThrow(new NotFoundException("Book not found with ID 99"));

        NestedServletException exception = assertThrows(NestedServletException.class,
                () -> mockMvc.perform(get("/book/99")));

        assertTrue(exception.getCause() instanceof NotFoundException);
    }

    @Test
    @DisplayName("GET /add shows the form with categories, authors and publishers")
    void showCreateForm() throws Exception {
        mockMvc.perform(get("/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("add-book"))
                .andExpect(model().attributeExists("book", "categories", "authors", "publishers"));

        verify(categoryService).findAllCategories();
        verify(authorService).findAllAuthors();
        verify(publisherService).findAllPublishers();
    }

    @Test
    @DisplayName("POST /add-book with valid data creates the book and redirects")
    void createBookWithValidData() throws Exception {
        mockMvc.perform(post("/add-book")
                        .param("isbn", "978-0132350884")
                        .param("name", "Clean Code")
                        .param("serialName", "Robert C. Martin Series")
                        .param("description", "A handbook of agile software craftsmanship"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));

        ArgumentCaptor<Book> bookCaptor = ArgumentCaptor.forClass(Book.class);
        verify(bookService).createBook(bookCaptor.capture());
        Book createdBook = bookCaptor.getValue();
        assertEquals("978-0132350884", createdBook.getIsbn());
        assertEquals("Clean Code", createdBook.getName());
        assertEquals("Robert C. Martin Series", createdBook.getSerialName());
    }

    @Test
    @DisplayName("POST /add-book with binding error returns the form and does not create a book")
    void createBookWithInvalidData() throws Exception {
        mockMvc.perform(post("/add-book").param("id", "abc").param("name", "Clean Code"))
                .andExpect(status().isOk())
                .andExpect(view().name("add-book"))
                .andExpect(model().attributeHasFieldErrors("book", "id"));

        verify(bookService, never()).createBook(any(Book.class));
    }

    @Test
    @DisplayName("GET /update/{id} shows the edit form")
    void showUpdateForm() throws Exception {
        Book book = createTestBook(1L, "111", "Clean Code");
        when(bookService.findBookById(1L)).thenReturn(book);

        mockMvc.perform(get("/update/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("update-book"))
                .andExpect(model().attribute("book", book));
    }

    @Test
    @DisplayName("POST /update-book/{id} updates the book with the ID from the URL")
    void updateBookWithValidData() throws Exception {
        mockMvc.perform(post("/update-book/5")
                        .param("isbn", "555")
                        .param("name", "Changed name")
                        .param("serialName", "Changed serial")
                        .param("description", "Changed description"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));

        ArgumentCaptor<Book> bookCaptor = ArgumentCaptor.forClass(Book.class);
        verify(bookService).updateBook(bookCaptor.capture());
        Book updatedBook = bookCaptor.getValue();
        assertEquals(5L, updatedBook.getId());
        assertEquals("Changed name", updatedBook.getName());
    }

    @Test
    @DisplayName("POST /update-book/{id} with error returns the form and sets the ID")
    void updateBookWithInvalidData() throws Exception {
        mockMvc.perform(post("/update-book/5").param("id", "abc"))
                .andExpect(status().isOk())
                .andExpect(view().name("update-book"))
                .andExpect(model().attribute("book", hasProperty("id", equalTo(5L))));

        verify(bookService, never()).updateBook(any(Book.class));
    }

    @Test
    @DisplayName("GET /remove-book/{id} deletes the book and redirects")
    void deleteBookWithExistingId() throws Exception {
        mockMvc.perform(get("/remove-book/3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));

        verify(bookService).deleteBook(3L);
    }

    @Test
    @DisplayName("GET /remove-book/{id} for non-existing book: exception is not handled")
    void deleteBookWithNonExistingId() {
        doThrow(new NotFoundException("Book not found with ID 99")).when(bookService).deleteBook(99L);

        NestedServletException exception = assertThrows(NestedServletException.class,
                () -> mockMvc.perform(get("/remove-book/99")));

        assertTrue(exception.getCause() instanceof NotFoundException);
    }
}