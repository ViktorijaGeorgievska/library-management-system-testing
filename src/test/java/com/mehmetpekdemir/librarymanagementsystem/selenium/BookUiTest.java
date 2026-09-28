package com.mehmetpekdemir.librarymanagementsystem.selenium;

import org.junit.jupiter.api.*;
import com.mehmetpekdemir.librarymanagementsystem.selenium.pages.BookDetailsPage;
import com.mehmetpekdemir.librarymanagementsystem.selenium.pages.BookFormPage;
import com.mehmetpekdemir.librarymanagementsystem.selenium.pages.BooksListPage;
import com.mehmetpekdemir.librarymanagementsystem.selenium.pages.NavigationBar;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BookUiTest extends BaseSeleniumTest {
    @Test
    @DisplayName("Books page shows the three initial books")
    public void booksPage() {
        BooksListPage booksPage = new BooksListPage(driver);
        booksPage.open(baseUrl);

        assertEquals("All Books", driver.getTitle());
        List<String> names = booksPage.getBookNames();
        assertTrue(names.contains("Test name"));
        assertTrue(names.contains("Test name1"));
        assertTrue(names.contains("Test name2"));
    }

    @Test
    @DisplayName("Search with existing keyword shows only the matching book")
    void searchExistingKeyword() {
        BooksListPage booksPage = new BooksListPage(driver);
        booksPage.open(baseUrl);

        new NavigationBar(driver).searchFor("name1");
        waitUntilUrlContains("/searchBook");

        List<String> names = booksPage.getBookNames();
        assertEquals(1, names.size());
        assertEquals("Test name1", names.get(0));
    }

    @Test
    @DisplayName("Search with unknown keyword shows empty table")
    void searchUnknownKeyword() {
        BooksListPage booksPage = new BooksListPage(driver);
        booksPage.open(baseUrl);

        new NavigationBar(driver).searchFor("noSuchBook");
        waitUntilUrlContains("/searchBook");

        assertEquals(0, booksPage.getNumberOfBooks());
    }

    @Test
    @DisplayName("Details page shows the category and the author")
    void detailsPage() {
        BooksListPage booksPage = new BooksListPage(driver);
        booksPage.open(baseUrl);

        booksPage.clickDetailsOfBook("Test name");
        waitUntilUrlContains("/book/");

        BookDetailsPage detailsPage = new BookDetailsPage(driver);
        assertEquals("Test isbn", detailsPage.getIsbn());
        assertEquals("Test category name", detailsPage.getCategoryName());
        assertEquals("Test name", detailsPage.getBookName());
        assertEquals("Test author name", detailsPage.getAuthorName());
    }

    @Test
    @DisplayName("Add book: new book appears in the list")
    void addBook() {
        String bookName = uniqueText("Selenium Book");
        BooksListPage booksPage = new BooksListPage(driver);
        booksPage.open(baseUrl);

        booksPage.clickAddBook();
        waitUntilTitleIs("Add Book");

        BookFormPage formPage = new BookFormPage(driver);
        formPage.fillIsbn(uniqueText("SEL"));
        formPage.fillName(bookName);
        formPage.fillSerialName("Selenium serial");
        formPage.fillDescription("Added by Selenium");
        formPage.submit();
        waitUntilTitleIs("All Books");

        assertTrue(driver.getCurrentUrl().endsWith("/books"));
        assertTrue(booksPage.getBookNames().contains(bookName));
        assertEquals("Added by Selenium", booksPage.getDescriptionOfBook(bookName));
    }

    @Test
    @DisplayName("Edit book: new description is shown in the list")
    void updateBook() {
        String bookName = uniqueText("Book to edit");
        BooksListPage booksPage = new BooksListPage(driver);
        booksPage.open(baseUrl);
        booksPage.clickAddBook();
        waitUntilTitleIs("Add Book");
        BookFormPage formPage = new BookFormPage(driver);
        formPage.fillIsbn(uniqueText("EDIT"));
        formPage.fillName(bookName);
        formPage.fillSerialName("Serial");
        formPage.fillDescription("Old description");
        formPage.submit();
        waitUntilTitleIs("All Books");

        booksPage.clickEditOfBook(bookName);
        waitUntilTitleIs("Update Book");
        BookFormPage editPage = new BookFormPage(driver);
        editPage.fillDescription("New description");
        editPage.submit();
        waitUntilTitleIs("All Books");

        assertEquals("New description", booksPage.getDescriptionOfBook(bookName));
    }
}
