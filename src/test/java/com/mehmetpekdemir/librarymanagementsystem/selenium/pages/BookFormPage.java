package com.mehmetpekdemir.librarymanagementsystem.selenium.pages;

import org.openqa.selenium.*;

public class BookFormPage {
    private final WebDriver driver;

    public BookFormPage(WebDriver driver) {
        this.driver = driver;
    }

    public void fillIsbn(String isbn) {
        fillField("isbn", isbn);
    }

    public void fillName(String name) {
        fillField("name", name);
    }

    public void fillSerialName(String serialName) {
        fillField("serialName", serialName);
    }

    public void fillDescription(String description) {
        fillField("description", description);
    }

    public void submit() {
        driver.findElement(By.cssSelector("form[method='post'] input[type='submit']")).click();
    }

    private void fillField(String fieldName, String value) {
        WebElement field = driver.findElement(By.name(fieldName));
        field.clear();
        field.sendKeys(value);
    }
}
