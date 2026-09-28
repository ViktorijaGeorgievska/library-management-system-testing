package com.mehmetpekdemir.librarymanagementsystem.selenium;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.support.ui.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

import java.time.Duration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.url=jdbc:h2:mem:seleniumdb")
public class BaseSeleniumTest {
    private static final boolean RUN_HEADLESS = false;

    @LocalServerPort
    protected int port;
    protected WebDriver driver;
    protected String baseUrl;

    @BeforeEach
    public void startBrowser() {
        ChromeOptions options = new ChromeOptions();

        if (RUN_HEADLESS) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--window-size=1400,1000");

        driver = new ChromeDriver(options);
        baseUrl = "http://localhost:" + port;
    }

    @AfterEach
    public void closeBrowser() {
        if (driver != null) {
            driver.quit();
        }
    }

    protected void waitUntilUrlContains(String partOfUrl) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

        wait.until(ExpectedConditions.urlContains(partOfUrl));
    }

    protected void waitUntilTitleIs(String title) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        wait.until(ExpectedConditions.titleIs(title));
    }

    protected String uniqueText(String prefix) {
        return prefix + " " + System.nanoTime();
    }
}
