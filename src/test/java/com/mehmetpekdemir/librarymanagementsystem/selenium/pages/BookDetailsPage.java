package com.mehmetpekdemir.librarymanagementsystem.selenium.pages;

import org.openqa.selenium.*;

import java.util.List;

public class BookDetailsPage {
    private final WebDriver driver;

    public BookDetailsPage(WebDriver driver) {
        this.driver = driver;
    }

    public String getIsbn() {
        return getCell(0);
    }

    public String getCategoryName() {
        return getCell(1);
    }

    public String getBookName() {
        return getCell(2);
    }

    public String getAuthorName() {
        return getCell(3);
    }

    private String getCell(int index) {
        List<WebElement> cells = driver.findElements(By.cssSelector("table tbody tr td"));
        return cells.get(index).getText();
    }
}
