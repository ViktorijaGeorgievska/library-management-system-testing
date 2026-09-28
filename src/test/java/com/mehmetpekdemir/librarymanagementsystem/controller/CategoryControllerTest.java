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

import com.mehmetpekdemir.librarymanagementsystem.entity.Category;
import com.mehmetpekdemir.librarymanagementsystem.exception.NotFoundException;
import com.mehmetpekdemir.librarymanagementsystem.service.CategoryService;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {
    @Mock
    private CategoryService categoryService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        CategoryController categoryController = new CategoryController(categoryService);

        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/");
        viewResolver.setSuffix(".html");

        mockMvc = MockMvcBuilders.standaloneSetup(categoryController).setViewResolvers(viewResolver).build();
    }

    private Category createTestCategory(Long id, String name) {
        Category category = new Category(name);
        category.setId(id);
        return category;
    }

    @Test
    @DisplayName("GET /categories shows the list")
    void getCategories() throws Exception {
        List<Category> items = new ArrayList<>();
        items.add(createTestCategory(1L, "First"));
        items.add(createTestCategory(2L, "Second"));
        when(categoryService.findAllCategories()).thenReturn(items);

        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(view().name("list-categories"))
                .andExpect(model().attribute("categories", items));
    }

    @Test
    @DisplayName("DEFECT: GET /category/{id} returns view 'list-category' but no such template exists")
    void getByExistingId() throws Exception {
        Category category = createTestCategory(1L, "First");
        when(categoryService.findCategoryById(1L)).thenReturn(category);

        mockMvc.perform(get("/category/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("list-category"))
                .andExpect(model().attribute("category", category));
    }

    @Test
    @DisplayName("GET /category/{id} for non-existing record: exception is not handled")
    void getByNonExistingId() {
        when(categoryService.findCategoryById(99L)).thenThrow(new NotFoundException("not found"));

        NestedServletException exception = assertThrows(NestedServletException.class,
                () -> mockMvc.perform(get("/category/99")));

        assertTrue(exception.getCause() instanceof NotFoundException);
    }

    @Test
    @DisplayName("GET /addCategory shows the empty form")
    void showCreateForm() throws Exception {
        mockMvc.perform(get("/addCategory"))
                .andExpect(status().isOk())
                .andExpect(view().name("add-category"))
                .andExpect(model().attributeExists("category"));
    }

    @Test
    @DisplayName("POST /add-category with valid data creates a record and redirects")
    void createWithValidData() throws Exception {
        mockMvc.perform(post("/add-category")
                        .param("name", "New name"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/categories"));

        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(categoryService).createCategory(captor.capture());
        assertEquals("New name", captor.getValue().getName());
    }

    @Test
    @DisplayName("POST /add-category with binding error returns the form")
    void createWithInvalidData() throws Exception {
        mockMvc.perform(post("/add-category").param("id", "abc"))
                .andExpect(status().isOk())
                .andExpect(view().name("add-category"))
                .andExpect(model().attributeHasFieldErrors("category", "id"));

        verify(categoryService, never()).createCategory(any(Category.class));
    }

    @Test
    @DisplayName("GET /updateCategory/{id} shows the edit form")
    void showUpdateForm() throws Exception {
        Category category = createTestCategory(1L, "First");
        when(categoryService.findCategoryById(1L)).thenReturn(category);

        mockMvc.perform(get("/updateCategory/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("update-category"))
                .andExpect(model().attribute("category", category));
    }

    @Test
    @DisplayName("POST /update-category/{id} updates with the ID from the URL and redirects")
    void updateWithValidData() throws Exception {
        mockMvc.perform(post("/update-category/5")
                        .param("name", "Changed name"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/categories"));

        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(categoryService).updateCategory(captor.capture());
        assertEquals(5L, captor.getValue().getId());
        assertEquals("Changed name", captor.getValue().getName());
    }

    @Test
    @DisplayName("POST /update-category/{id} with error returns the form and sets the ID")
    void updateWithInvalidData() throws Exception {
        mockMvc.perform(post("/update-category/5").param("id", "abc"))
                .andExpect(status().isOk())
                .andExpect(view().name("update-category"))
                .andExpect(model().attribute("category", hasProperty("id", equalTo(5L))));

        verify(categoryService, never()).updateCategory(any(Category.class));
    }

    @Test
    @DisplayName("GET /remove-category/{id} deletes and redirects")
    void deleteExistingId() throws Exception {
        mockMvc.perform(get("/remove-category/3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/categories"));

        verify(categoryService).deleteCategory(3L);
    }

    @Test
    @DisplayName("GET /remove-category/{id} for non-existing record: exception is not handled")
    void deleteNonExistingId() {
        doThrow(new NotFoundException("not found")).when(categoryService).deleteCategory(99L);

        NestedServletException exception = assertThrows(NestedServletException.class,
                () -> mockMvc.perform(get("/remove-category/99")));

        assertTrue(exception.getCause() instanceof NotFoundException);
    }
}