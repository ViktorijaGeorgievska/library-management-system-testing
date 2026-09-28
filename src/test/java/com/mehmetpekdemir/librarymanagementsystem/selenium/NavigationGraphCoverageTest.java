package com.mehmetpekdemir.librarymanagementsystem.selenium;

import com.mehmetpekdemir.librarymanagementsystem.selenium.pages.BookFormPage;
import com.mehmetpekdemir.librarymanagementsystem.selenium.pages.BooksListPage;
import com.mehmetpekdemir.librarymanagementsystem.selenium.pages.NavigationBar;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class NavigationGraphCoverageTest extends BaseSeleniumTest {
    private void assertOnPage(String expectedTitle, String expectedUrlPart) {
        waitUntilUrlContains(expectedUrlPart);
        assertEquals(expectedTitle, driver.getTitle());
        assertTrue(driver.getCurrentUrl().contains(expectedUrlPart));
    }

    @Test
    @DisplayName("TP1: N1 → N2 → N3 → N2 → N4 → N2 → N5 → N1")
    void testPath1_addEditDetailsAndHome() {
        NavigationBar navigationBar = new NavigationBar(driver);
        BooksListPage booksPage = new BooksListPage(driver);
        BookFormPage formPage = new BookFormPage(driver);

        String bookName = uniqueText("Graph book");

        driver.get(baseUrl + "/");
        assertEquals("Home Page", driver.getTitle());

        navigationBar.clickBooks();
        assertOnPage("All Books", "/books");

        booksPage.clickAddBook();
        assertOnPage("Add Book", "/add");

        formPage.fillIsbn(uniqueText("GRAPH"));
        formPage.fillName(bookName);
        formPage.fillSerialName("Graph serial");
        formPage.fillDescription("Graph description");
        formPage.submit();
        assertOnPage("All Books", "/books");

        booksPage.clickEditOfBook(bookName);
        assertOnPage("Update Book", "/update/");

        formPage.fillDescription("Graph description edited");
        formPage.submit();
        assertOnPage("All Books", "/books");

        booksPage.clickDetailsOfBook("Test name");
        assertOnPage("All Books", "/book/");

        navigationBar.clickHome();
        waitUntilTitleIs("Home Page");
        assertEquals("Home Page", driver.getTitle());
    }

    @Test
    @DisplayName("TP2: N1 → N2 → N6 → N6 → N2")
    void testPath2_searchTwiceAndBack() {
        NavigationBar navigationBar = new NavigationBar(driver);
        BooksListPage booksPage = new BooksListPage(driver);

        driver.get(baseUrl + "/");
        assertEquals("Home Page", driver.getTitle());

        navigationBar.clickBooks();
        assertOnPage("All Books", "/books");

        navigationBar.searchFor("name1");
        assertOnPage("All Books", "/searchBook");
        assertEquals(1, booksPage.getNumberOfBooks());

        navigationBar.searchFor("no-such-book-xyz");
        assertOnPage("All Books", "keyword=no-such-book-xyz");
        assertEquals(0, booksPage.getNumberOfBooks());

        navigationBar.clickBooks();
        assertOnPage("All Books", "/books");
        assertTrue(booksPage.getNumberOfBooks() >= 3);
    }
}
