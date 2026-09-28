package com.mehmetpekdemir.librarymanagementsystem.selenium;

import org.junit.jupiter.api.*;
import com.mehmetpekdemir.librarymanagementsystem.selenium.pages.CategoriesPage;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CategoryUiTest extends BaseSeleniumTest {
    @Test
    @DisplayName("Add category: appears in the list")
    void addCategory_appearsInList() {
        String categoryName = uniqueText("Category");
        CategoriesPage categoriesPage = new CategoriesPage(driver);
        categoriesPage.open(baseUrl);

        categoriesPage.clickAddCategory();
        waitUntilTitleIs("Add Category");
        categoriesPage.fillNameAndSubmit(categoryName);
        waitUntilTitleIs("All Categories");

        assertTrue(categoriesPage.getCategoryNames().contains(categoryName));
    }

    @Test
    @DisplayName("Delete category: disappears from the list")
    void deleteCategory_disappearsFromList() {
        String categoryName = uniqueText("To delete");
        CategoriesPage categoriesPage = new CategoriesPage(driver);
        categoriesPage.open(baseUrl);
        categoriesPage.clickAddCategory();
        waitUntilTitleIs("Add Category");
        categoriesPage.fillNameAndSubmit(categoryName);
        waitUntilTitleIs("All Categories");
        assertTrue(categoriesPage.getCategoryNames().contains(categoryName));

        categoriesPage.clickDeleteOfCategory(categoryName);
        waitUntilTitleIs("All Categories");

        assertFalse(categoriesPage.getCategoryNames().contains(categoryName));
    }
}
