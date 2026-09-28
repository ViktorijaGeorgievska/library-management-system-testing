package com.mehmetpekdemir.librarymanagementsystem.selenium.pages;

import org.openqa.selenium.*;

public class NavigationBar {
    private final WebDriver driver;

    public NavigationBar(WebDriver driver) {
        this.driver = driver;
    }

    public void clickHome() {
        driver.findElement(By.cssSelector("a.nav-link[href='/']")).click();
    }

    public void clickCategories() {
        driver.findElement(By.cssSelector("a.nav-link[href='/categories']")).click();
    }

    public void clickBooks() {
        driver.findElement(By.cssSelector("a.nav-link[href='/books']")).click();
    }

    public void clickPublishers() {
        driver.findElement(By.cssSelector("a.nav-link[href='/publishers']")).click();
    }

    public void clickAuthors() {
        driver.findElement(By.cssSelector("a.nav-link[href='/authors']")).click();
    }

    public void searchFor(String keyword) {
        WebElement searchInput = driver.findElement(By.id("keyword"));
        searchInput.clear();
        searchInput.sendKeys(keyword);
        driver.findElement(By.cssSelector("input[type='submit'][value='Search']")).click();
    }
}
