package com.mehmetpekdemir.librarymanagementsystem.selenium.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;

import java.util.*;

public class BooksListPage {
    private final WebDriver driver;

    public BooksListPage(WebDriver driver) {
        this.driver = driver;
    }

    public void open(String baseUrl) {
        driver.get(baseUrl + "/books");
    }

    private List<WebElement> getRows() {
        return driver.findElements(By.cssSelector("table tbody tr"));
    }

    private WebElement findRowByBookName(String bookName) {
        for (WebElement row : getRows()) {
            List<WebElement> cells = row.findElements(By.tagName("td"));
            if (cells.get(1).getText().equals(bookName)) {
                return row;
            }
        }
        throw new NoSuchElementException("Book not found in table: " + bookName);
    }

    public int getNumberOfBooks() {
        return getRows().size();
    }

    public List<String> getBookNames() {
        List<String> names = new ArrayList<>();
        for (WebElement row : getRows()) {
            List<WebElement> cells = row.findElements(By.tagName("td"));
            names.add(cells.get(1).getText());
        }
        return names;
    }

    public String getDescriptionOfBook(String bookName) {
        WebElement row = findRowByBookName(bookName);
        List<WebElement> cells = row.findElements(By.tagName("td"));
        return cells.get(3).getText();
    }

    public void clickAddBook() {
        driver.findElement(By.cssSelector("a[href='/add']")).click();
    }

    public void clickDetailsOfBook(String bookName) {
        findRowByBookName(bookName).findElement(By.cssSelector("a[href^='/book/']")).click();
    }

    public void clickEditOfBook(String bookName) {
        findRowByBookName(bookName).findElement(By.cssSelector("a[href^='/update/']")).click();
    }
}
