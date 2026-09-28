package com.mehmetpekdemir.librarymanagementsystem.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
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

import com.mehmetpekdemir.librarymanagementsystem.entity.Author;
import com.mehmetpekdemir.librarymanagementsystem.exception.NotFoundException;
import com.mehmetpekdemir.librarymanagementsystem.service.AuthorService;

@ExtendWith(MockitoExtension.class)
class AuthorControllerTest {
    @Mock
    private AuthorService authorService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AuthorController authorController = new AuthorController(authorService);

        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/");
        viewResolver.setSuffix(".html");

        mockMvc = MockMvcBuilders.standaloneSetup(authorController).setViewResolvers(viewResolver).build();
    }

    private Author createTestAuthor(Long id, String name) {
        Author author = new Author(name, "Test description");
        author.setId(id);
        return author;
    }

    @Test
    @DisplayName("GET /authors shows the list")
    void getAuthors() throws Exception {
        List<Author> items = new ArrayList<>();
        items.add(createTestAuthor(1L, "First"));
        items.add(createTestAuthor(2L, "Second"));
        when(authorService.findAllAuthors()).thenReturn(items);

        mockMvc.perform(get("/authors"))
                .andExpect(status().isOk())
                .andExpect(view().name("list-authors"))
                .andExpect(model().attribute("authors", items));
    }

    @Test
    @DisplayName("DEFECT: GET /author/{id} returns view 'list-author' but no such template exists")
    void getByExistingId() throws Exception {
        Author author = createTestAuthor(1L, "First");
        when(authorService.findAuthorById(1L)).thenReturn(author);

        mockMvc.perform(get("/author/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("list-author"))
                .andExpect(model().attribute("author", author));
    }

    @Test
    @DisplayName("GET /author/{id} for non-existing record: exception is not handled")
    void getByNonExistingId() {
        when(authorService.findAuthorById(99L)).thenThrow(new NotFoundException("not found"));

        NestedServletException exception = assertThrows(NestedServletException.class,
                () -> mockMvc.perform(get("/author/99")));

        assertTrue(exception.getCause() instanceof NotFoundException);
    }

    @Test
    @DisplayName("GET /addAuthor shows the empty form")
    void showCreateForm() throws Exception {
        mockMvc.perform(get("/addAuthor"))
                .andExpect(status().isOk())
                .andExpect(view().name("add-author"))
                .andExpect(model().attributeExists("author"));
    }

    @Test
    @DisplayName("POST /add-author with valid data creates a record and redirects")
    void createWithValidData() throws Exception {
        mockMvc.perform(post("/add-author")
                        .param("name", "New name")
                        .param("description", "New description"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/authors"));

        ArgumentCaptor<Author> captor = ArgumentCaptor.forClass(Author.class);
        verify(authorService).createAuthor(captor.capture());
        assertEquals("New name", captor.getValue().getName());
    }

    @Test
    @DisplayName("POST /add-author with binding error returns the form")
    void createWithInvalidData() throws Exception {
        mockMvc.perform(post("/add-author").param("id", "abc"))
                .andExpect(status().isOk())
                .andExpect(view().name("add-author"))
                .andExpect(model().attributeHasFieldErrors("author", "id"));

        verify(authorService, never()).createAuthor(any(Author.class));
    }

    @Test
    @DisplayName("GET /updateAuthor/{id} shows the edit form")
    void showUpdateForm() throws Exception {
        Author author = createTestAuthor(1L, "First");
        when(authorService.findAuthorById(1L)).thenReturn(author);

        mockMvc.perform(get("/updateAuthor/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("update-author"))
                .andExpect(model().attribute("author", author));
    }

    @Test
    @DisplayName("POST /update-author/{id} updates with the ID from the URL and redirects")
    void updateWithValidData() throws Exception {
        mockMvc.perform(post("/update-author/5")
                        .param("name", "Changed name")
                        .param("description", "Changed description"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/authors"));

        ArgumentCaptor<Author> captor = ArgumentCaptor.forClass(Author.class);
        verify(authorService).updateAuthor(captor.capture());
        assertEquals(5L, captor.getValue().getId());
        assertEquals("Changed name", captor.getValue().getName());
    }

    @Test
    @DisplayName("POST /update-author/{id} with error returns the form and sets the ID")
    void updateWithInvalidData() throws Exception {
        mockMvc.perform(post("/update-author/5").param("id", "abc"))
                .andExpect(status().isOk())
                .andExpect(view().name("update-author"))
                .andExpect(model().attribute("author", hasProperty("id", equalTo(5L))));

        verify(authorService, never()).updateAuthor(any(Author.class));
    }

    @Test
    @DisplayName("GET /remove-author/{id} deletes and redirects")
    void deleteWithExistingId() throws Exception {
        mockMvc.perform(get("/remove-author/3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/authors"));

        verify(authorService).deleteAuthor(3L);
    }

    @Test
    @DisplayName("GET /remove-author/{id} for non-existing record: exception is not handled")
    void deleteWithNonExistingId() {
        doThrow(new NotFoundException("not found")).when(authorService).deleteAuthor(99L);

        NestedServletException exception = assertThrows(NestedServletException.class,
                () -> mockMvc.perform(get("/remove-author/99")));

        assertTrue(exception.getCause() instanceof NotFoundException);
    }
}