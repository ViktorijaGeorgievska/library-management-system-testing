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

import com.mehmetpekdemir.librarymanagementsystem.entity.Publisher;
import com.mehmetpekdemir.librarymanagementsystem.exception.NotFoundException;
import com.mehmetpekdemir.librarymanagementsystem.service.PublisherService;

@ExtendWith(MockitoExtension.class)
class PublisherControllerTest {
    @Mock
    private PublisherService publisherService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        PublisherController publisherController = new PublisherController(publisherService);

        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/");
        viewResolver.setSuffix(".html");

        mockMvc = MockMvcBuilders.standaloneSetup(publisherController).setViewResolvers(viewResolver).build();
    }

    private Publisher createTestPublisher(Long id, String name) {
        Publisher publisher = new Publisher(name);
        publisher.setId(id);
        return publisher;
    }

    @Test
    @DisplayName("GET /publishers shows the list")
    void getPublishers() throws Exception {
        List<Publisher> items = new ArrayList<>();
        items.add(createTestPublisher(1L, "First"));
        items.add(createTestPublisher(2L, "Second"));
        when(publisherService.findAllPublishers()).thenReturn(items);

        mockMvc.perform(get("/publishers"))
                .andExpect(status().isOk())
                .andExpect(view().name("list-publishers"))
                .andExpect(model().attribute("publishers", items));
    }

    @Test
    @DisplayName("DEFECT: GET /publisher/{id} returns view 'list-publisher' but no such template exists")
    void getByExistingId() throws Exception {
        Publisher publisher = createTestPublisher(1L, "First");
        when(publisherService.findPublisherById(1L)).thenReturn(publisher);

        mockMvc.perform(get("/publisher/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("list-publisher"))
                .andExpect(model().attribute("publisher", publisher));
    }

    @Test
    @DisplayName("GET /publisher/{id} for non-existing record: exception is not handled")
    void getByNonExistingId() {
        when(publisherService.findPublisherById(99L)).thenThrow(new NotFoundException("not found"));

        NestedServletException exception = assertThrows(NestedServletException.class,
                () -> mockMvc.perform(get("/publisher/99")));

        assertTrue(exception.getCause() instanceof NotFoundException);
    }

    @Test
    @DisplayName("GET /addPublisher shows the empty form")
    void showCreateForm() throws Exception {
        mockMvc.perform(get("/addPublisher"))
                .andExpect(status().isOk())
                .andExpect(view().name("add-publisher"))
                .andExpect(model().attributeExists("publisher"));
    }

    @Test
    @DisplayName("POST /add-publisher with valid data creates a record and redirects")
    void createWithValidData() throws Exception {
        mockMvc.perform(post("/add-publisher")
                        .param("name", "New name"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/publishers"));

        ArgumentCaptor<Publisher> captor = ArgumentCaptor.forClass(Publisher.class);
        verify(publisherService).createPublisher(captor.capture());
        assertEquals("New name", captor.getValue().getName());
    }

    @Test
    @DisplayName("POST /add-publisher with binding error returns the form")
    void createWithInvalidData() throws Exception {
        mockMvc.perform(post("/add-publisher").param("id", "abc"))
                .andExpect(status().isOk())
                .andExpect(view().name("add-publisher"))
                .andExpect(model().attributeHasFieldErrors("publisher", "id"));

        verify(publisherService, never()).createPublisher(any(Publisher.class));
    }

    @Test
    @DisplayName("GET /updatePublisher/{id} shows the edit form")
    void showUpdateForm() throws Exception {
        Publisher publisher = createTestPublisher(1L, "First");
        when(publisherService.findPublisherById(1L)).thenReturn(publisher);

        mockMvc.perform(get("/updatePublisher/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("update-publisher"))
                .andExpect(model().attribute("publisher", publisher));
    }

    @Test
    @DisplayName("POST /update-publisher/{id} updates with the ID from the URL and redirects")
    void updateWithValidData() throws Exception {
        mockMvc.perform(post("/update-publisher/5")
                        .param("name", "Changed name"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/publishers"));

        ArgumentCaptor<Publisher> captor = ArgumentCaptor.forClass(Publisher.class);
        verify(publisherService).updatePublisher(captor.capture());
        assertEquals(5L, captor.getValue().getId());
        assertEquals("Changed name", captor.getValue().getName());
    }

    @Test
    @DisplayName("DEFECT: POST /update-publisher/{id} with error returns 'update-publishers' (typo)")
    void updateWithInvalidData() throws Exception {
        mockMvc.perform(post("/update-publisher/5").param("id", "abc"))
                .andExpect(status().isOk())
                .andExpect(view().name("update-publishers"))
                .andExpect(model().attribute("publisher", hasProperty("id", equalTo(5L))));

        verify(publisherService, never()).updatePublisher(any(Publisher.class));
    }

    @Test
    @DisplayName("GET /remove-publisher/{id} deletes and redirects")
    void deleteExistingId() throws Exception {
        mockMvc.perform(get("/remove-publisher/3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/publishers"));

        verify(publisherService).deletePublisher(3L);
    }

    @Test
    @DisplayName("GET /remove-publisher/{id} for non-existing record: exception is not handled")
    void deleteNonExistingId() {
        doThrow(new NotFoundException("not found")).when(publisherService).deletePublisher(99L);

        NestedServletException exception = assertThrows(NestedServletException.class,
                () -> mockMvc.perform(get("/remove-publisher/99")));

        assertTrue(exception.getCause() instanceof NotFoundException);
    }
}