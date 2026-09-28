package com.mehmetpekdemir.librarymanagementsystem.selenium.pages;

import org.openqa.selenium.*;

import java.util.ArrayList;
import java.util.List;

public class CategoriesPage {
    private final WebDriver driver;

    public CategoriesPage(WebDriver driver) {
        this.driver = driver;
    }

    public void open(String baseUrl) {
        driver.get(baseUrl + "/categories");
    }

    private List<WebElement> getRows() {
        return driver.findElements(By.cssSelector("table tbody tr"));
    }

    private WebElement findRowByName(String categoryName) {
        for (WebElement row : getRows()) {
            if (row.findElements(By.tagName("td")).get(0).getText().equals(categoryName)) {
                return row;
            }
        }
        throw new NoSuchElementException("Category not found in table: " + categoryName);
    }

    public List<String> getCategoryNames() {
        List<String> names = new ArrayList<>();

        for (WebElement row : getRows()) {
            names.add(row.findElements(By.tagName("td")).get(0).getText());
        }
        return names;
    }

    public void clickAddCategory() {
        driver.findElement(By.cssSelector("a[href='/addCategory']")).click();
    }

    public void clickDeleteOfCategory(String categoryName) {
        findRowByName(categoryName).findElement(By.cssSelector("a[href^='/remove-category/']")).click();
    }

    public void fillNameAndSubmit(String categoryName) {
        WebElement nameField = driver.findElement(By.name("name"));
        nameField.clear();
        nameField.sendKeys(categoryName);
        driver.findElement(By.cssSelector("form[method='post'] input[type='submit']")).click();
    }
}
